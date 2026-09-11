package com.hopital.accounting.billing;

import static com.hopital.accounting.billing.BillingStore.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopital.accounting.application.domain.*;
import com.hopital.accounting.application.dto.PageResponse;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Pre-invoicing only: neither a payment nor a journal entry is created here. */
@Service
@Transactional(readOnly=true)
public class BillingService {
    private final BillingStore db;
    private final BillingReferences references;
    private final ObjectMapper json;
    private final ZoneId zone;
    public BillingService(BillingStore db,BillingReferences references,ObjectMapper json,
            @Value("${hospital.billing.zone:Africa/Kinshasa}") String zone) {
        this.db=db;this.references=references;this.json=json;this.zone=ZoneId.of(zone);
    }
    private LocalDate today(){return LocalDate.now(zone);}
    private static ResponseStatusException error(HttpStatus status,String message){return new ResponseStatusException(status,message);}
    private static void require(boolean condition,String message){if(!condition)throw error(HttpStatus.UNPROCESSABLE_ENTITY,message);}
    private static String text(String value,int max){String result=value==null?"":value.trim();require(result.length()<=max,"Le texte saisi est trop long.");return result;}
    private static String required(String value,int max){String result=text(value,max);require(!result.isBlank(),"Les informations obligatoires doivent être renseignées.");return result;}
    private static String choice(String value,String...values){require(value!=null&&List.of(values).contains(value),"Une valeur sélectionnée n'est pas valide.");return value;}
    private static String category(String value){return choice(value,"CONSULTATION","LABORATORY","CARE","HOSPITALIZATION","OTHER");}
    private static String currency(String value){return choice(value,"CDF","USD");}
    private static String code(String prefix){return prefix+UUID.randomUUID().toString().replace("-","").substring(0,20).toUpperCase(Locale.ROOT);}
    private static void bounds(int page,int size){require(page>=0&&size>=1&&size<=100,"La pagination demandée est invalide.");}
    private UUID hospital(UUID requested,DataAccessScope scope){
        if(scope.administrator()){require(requested!=null,"Sélectionnez un hôpital.");return requested;}
        if(scope.hospitalId()==null||(requested!=null&&!requested.equals(scope.hospitalId())))throw error(HttpStatus.FORBIDDEN,"Cet hôpital ne fait pas partie de votre affectation.");
        return scope.hospitalId();
    }
    private void access(UUID hospital,DataAccessScope scope){hospital(hospital,scope);}
    private Map<String,Object> service(UUID id,DataAccessScope scope,String lock){
        var row=db.one("SELECT * FROM billing_services WHERE id=:id "+lock,p("id",id));access(id(row,"hospitalId"),scope);return row;
    }
    public Map<String,Object> service(UUID id,DataAccessScope scope){return service(id,scope,"");}
    private PageResponse<Map<String,Object>> page(String select,String from,String order,MapSqlParameterSource params,int page,int size){
        bounds(page,size);long total=db.count("SELECT count(*) "+from,params);
        params.addValue("limit",size).addValue("offset",(long)page*size);
        return new PageResponse<>(db.rows(select+" "+from+" ORDER BY "+order+" LIMIT :limit OFFSET :offset",params),page,size,total,(int)((total+size-1)/size));
    }
    public PageResponse<Map<String,Object>> services(UUID hospital,int page,int size,String query,String category,Boolean active,DataAccessScope scope){
        var params=p("hospital",hospital(hospital,scope),"query","%"+text(query,200).toLowerCase(Locale.ROOT)+"%");
        String from="FROM billing_services WHERE hospital_id=:hospital AND (lower(name) LIKE :query OR lower(code) LIKE :query)";
        if(category!=null&&!category.isBlank()){from+=" AND category=:category";params.addValue("category",category(category));}
        if(active!=null){from+=" AND active=:active";params.addValue("active",active);}
        return page("SELECT *",from,"name,id",params,page,size);
    }
    private void uniqueName(UUID hospital,String name,UUID except){
        // Serialize catalogue names per hospital before checking the normalized unique index.
        db.rows("SELECT pg_advisory_xact_lock(hashtextextended(CAST(:hospital AS text),1))",p("hospital",hospital));
        if(db.count("SELECT count(*) FROM billing_services WHERE hospital_id=:hospital AND lower(btrim(name))=lower(:name) AND id<>:except",p("hospital",hospital,"name",name,"except",except))>0)
            throw error(HttpStatus.CONFLICT,"Une prestation porte déjà ce nom dans cet hôpital.");
    }
    private void event(UUID hospital,UUID service,String kind,Object details,AuditActor actor){
        try{db.update("INSERT INTO billing_catalog_events(id,hospital_id,service_id,kind,details,operator_id,operator_name) VALUES(:id,:hospital,:service,:kind,CAST(:details AS jsonb),:actor,:name)",
            p("id",UUID.randomUUID(),"hospital",hospital,"service",service,"kind",kind,"details",json.writeValueAsString(details),"actor",actor.userId(),"name",actor.username()));}
        catch(JsonProcessingException e){throw new IllegalStateException("Impossible d'enregistrer la trace de modification.",e);}
    }
    @Transactional
    public Map<String,Object> create(BillingRequests.Service r,DataAccessScope scope,AuditActor actor){
        UUID hospital=hospital(r.hospitalId(),scope),id=UUID.randomUUID();references.hospital(hospital);
        String name=required(r.name(),200);uniqueName(hospital,name,id);
        db.update("INSERT INTO billing_services(id,hospital_id,code,name,category,description,created_by_id,created_by) VALUES(:id,:hospital,:code,:name,:category,:description,:actor,:operator)",
            p("id",id,"hospital",hospital,"code",code("PRS-"),"name",name,"category",category(r.category()),"description",text(r.description(),1000),"actor",actor.userId(),"operator",actor.username()));
        var row=service(id,scope);event(hospital,id,"CREATED",row,actor);return row;
    }
    @Transactional
    public Map<String,Object> update(UUID id,BillingRequests.UpdateService r,DataAccessScope scope,AuditActor actor){
        // Always acquire the hospital/name lock before the row lock, as in create().
        // The reverse order could deadlock with a concurrent creation of the new name.
        var reference=service(id,scope);
        db.rows("SELECT pg_advisory_xact_lock(hashtextextended(CAST(:hospital AS text),1))",p("hospital",id(reference,"hospitalId")));
        var old=service(id,scope,"FOR UPDATE");UUID hospital=id(old,"hospitalId");String name=required(r.name(),200),reason=required(r.reason(),1000);
        if(((Number)old.get("version")).intValue()!=r.version())throw error(HttpStatus.CONFLICT,"La prestation a été modifiée. Actualisez avant de réessayer.");
        uniqueName(hospital,name,id);
        db.update("UPDATE billing_services SET name=:name,category=:category,description=:description,active=:active,version=version+1 WHERE id=:id",
            p("id",id,"name",name,"category",category(r.category()),"description",text(r.description(),1000),"active",r.active()));
        var row=service(id,scope);event(hospital,id,"UPDATED",Map.of("before",old,"after",row,"reason",reason),actor);return row;
    }
    public PageResponse<Map<String,Object>> tariffs(UUID id,int page,int size,DataAccessScope scope){
        service(id,scope);return page("SELECT *","FROM billing_tariffs WHERE service_id=:id","effective_from DESC,created_at DESC,id",p("id",id),page,size);
    }
    public PageResponse<Map<String,Object>> events(UUID id,int page,int size,DataAccessScope scope){
        service(id,scope);return page("SELECT id,kind,details::text AS details,operator_name,created_at","FROM billing_catalog_events WHERE service_id=:id","created_at DESC,id",p("id",id),page,size);
    }
    @Transactional
    public Map<String,Object> tariff(UUID serviceId,BillingRequests.Tariff r,DataAccessScope scope,AuditActor actor){
        var service=service(serviceId,scope,"FOR UPDATE");require(Boolean.TRUE.equals(service.get("active")),"Réactivez la prestation avant d'ajouter un tarif.");
        require(r.effectiveFrom()!=null&&!r.effectiveFrom().isBefore(today()),"Un tarif ne peut pas être antidaté.");
        String currency=currency(r.currency());price(r.unitPrice());UUID id=UUID.randomUUID(),hospital=id(service,"hospitalId");
        if(db.count("SELECT count(*) FROM billing_tariffs WHERE service_id=:service AND currency=:currency AND effective_from=:date AND cancelled_at IS NULL",p("service",serviceId,"currency",currency,"date",r.effectiveFrom()))>0)
            throw error(HttpStatus.CONFLICT,"Un tarif existe déjà pour cette date et cette devise.");
        db.update("INSERT INTO billing_tariffs(id,service_id,hospital_id,effective_from,currency,unit,unit_price,reason,created_by_id,created_by) VALUES(:id,:service,:hospital,:date,:currency,:unit,:price,:reason,:actor,:name)",
            p("id",id,"service",serviceId,"hospital",hospital,"date",r.effectiveFrom(),"currency",currency,"unit",choice(r.unit(),"ACT","DAY","NIGHT","HOUR","PACKAGE"),"price",r.unitPrice(),"reason",required(r.reason(),1000),"actor",actor.userId(),"name",actor.username()));
        var row=db.one("SELECT * FROM billing_tariffs WHERE id=:id",p("id",id));event(hospital,serviceId,"TARIFF_ADDED",row,actor);return row;
    }
    @Transactional
    public Map<String,Object> cancelTariff(UUID serviceId,UUID tariffId,String reason,DataAccessScope scope,AuditActor actor){
        var service=service(serviceId,scope,"FOR UPDATE");var row=db.one("SELECT * FROM billing_tariffs WHERE id=:id AND service_id=:service",p("id",tariffId,"service",serviceId));
        reason=required(reason,1000);if(row.get("cancelledAt")!=null)return row;
        require(LocalDate.parse(row.get("effectiveFrom").toString()).isAfter(today()),"Seul un tarif futur peut être annulé. Ajoutez un nouveau tarif pour les prochaines prestations.");
        require(db.count("SELECT count(*) FROM billing_passage_charges WHERE tariff_id=:id",p("id",tariffId))==0,"Ce tarif a déjà été utilisé.");
        db.update("UPDATE billing_tariffs SET cancelled_at=now(),cancelled_by=:actor,cancellation_reason=:reason WHERE id=:id",p("id",tariffId,"actor",actor.username(),"reason",reason));
        event(id(service,"hospitalId"),serviceId,"TARIFF_CANCELLED",Map.of("tariffId",tariffId,"reason",reason),actor);
        return db.one("SELECT * FROM billing_tariffs WHERE id=:id",p("id",tariffId));
    }
    public BillingReferences.Passages passages(UUID hospital,int page,int size,String query,DataAccessScope scope){bounds(page,size);return references.passages(hospital(hospital,scope),page,size,text(query,200));}
    private BillingReferences.Passage passage(UUID id,DataAccessScope scope){var passage=references.passage(id);access(passage.hospitalId(),scope);return passage;}
    private void performed(BillingReferences.Passage passage,LocalDate date){
        require(!"CANCELLED".equals(passage.status()),"Un passage annulé ne peut pas recevoir de frais.");
        require(date!=null&&!date.isAfter(today())&&!date.isBefore(passage.arrivedAt().atZone(zone).toLocalDate())
            &&(passage.closedAt()==null||!date.isAfter(passage.closedAt().atZone(zone).toLocalDate())),"La date de réalisation doit se situer dans la période du passage, sans dépasser aujourd'hui.");
    }
    private static void price(BigDecimal price){require(price!=null&&price.signum()>=0&&price.stripTrailingZeros().scale()<=2&&price.compareTo(new BigDecimal("99999999999999.99"))<=0,"Le prix doit être positif ou nul, avec au plus deux décimales.");}
    public static BigDecimal amount(BigDecimal quantity,BigDecimal price){
        require(quantity!=null&&quantity.signum()>0&&quantity.stripTrailingZeros().scale()<=3&&quantity.compareTo(new BigDecimal("999999.999"))<=0,"La quantité doit être positive, avec au plus trois décimales.");
        price(price);BigDecimal total=quantity.multiply(price).setScale(2,RoundingMode.HALF_UP);
        require(total.compareTo(new BigDecimal("99999999999999.99"))<=0,"Le montant de la ligne dépasse la limite autorisée.");return total;
    }
    private Map<String,Object> quote(BillingReferences.Passage passage,UUID serviceId,LocalDate date,BigDecimal quantity,String currency,DataAccessScope scope){
        performed(passage,date);var service=service(serviceId,scope,"FOR SHARE");
        require(passage.hospitalId().equals(id(service,"hospitalId")),"La prestation doit appartenir à l'hôpital de ce passage.");
        require(Boolean.TRUE.equals(service.get("active")),"Cette prestation n'est plus active.");
        var tariffs=db.rows("SELECT * FROM billing_tariffs WHERE service_id=:service AND currency=:currency AND effective_from<=:date AND cancelled_at IS NULL ORDER BY effective_from DESC LIMIT 1",
            p("service",serviceId,"currency",currency(currency),"date",date));
        require(!tariffs.isEmpty(),"Aucun tarif applicable à cette date et dans cette devise. Contactez le gestionnaire des tarifs.");
        var rate=tariffs.get(0);return Map.of("service",service,"tariff",rate,"quantity",quantity,"totalAmount",amount(quantity,(BigDecimal)rate.get("unitPrice")));
    }
    // FOR SHARE needs a writable transaction in PostgreSQL, but this endpoint does not change data.
    @Transactional
    public Map<String,Object> quote(UUID passage,UUID service,LocalDate date,BigDecimal quantity,String currency,DataAccessScope scope){return quote(passage(passage,scope),service,date,quantity,currency,scope);}
    public Map<String,Object> workspace(UUID passageId,int page,int size,String query,String status,DataAccessScope scope){
        var passage=passage(passageId,scope);var params=p("passage",passageId,"query","%"+text(query,200).toLowerCase(Locale.ROOT)+"%");
        String from="FROM billing_passage_charges WHERE passage_id=:passage AND (lower(service_name) LIKE :query OR lower(code) LIKE :query)";
        if(status!=null&&!status.isBlank()){from+=" AND status=:status";params.addValue("status",choice(status,"UNBILLED","CANCELLED"));}
        return Map.of("passage",passage,"charges",page("SELECT *",from,"performed_on DESC,created_at DESC,id",params,page,size),
            "totals",db.rows("SELECT currency,sum(total_amount) AS amount,count(*) AS line_count FROM billing_passage_charges WHERE passage_id=:passage AND status='UNBILLED' GROUP BY currency ORDER BY currency",p("passage",passageId)));
    }
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String,Object> charge(UUID passageId,BillingRequests.Charge r,DataAccessScope scope,AuditActor actor){
        var passage=passage(passageId,scope);require(r.requestId()!=null,"La clé de la demande est obligatoire.");String note=required(r.note(),1000);
        db.rows("SELECT pg_advisory_xact_lock(hashtextextended(:key,2))",p("key",passage.hospitalId()+":"+r.requestId()));
        var existing=db.rows("SELECT * FROM billing_passage_charges WHERE hospital_id=:hospital AND request_id=:request",p("hospital",passage.hospitalId(),"request",r.requestId()));
        if(!existing.isEmpty()){
            var row=existing.get(0);
            if(!passageId.equals(id(row,"passageId"))||!Objects.equals(r.serviceId(),id(row,"serviceId"))||!Objects.equals(r.tariffId(),id(row,"tariffId"))
                ||!Objects.equals(r.currency(),row.get("currency"))||!Objects.equals(String.valueOf(r.performedOn()),row.get("performedOn"))
                ||r.quantity()==null||r.quantity().compareTo((BigDecimal)row.get("quantity"))!=0||!note.equals(row.get("note")))
                throw error(HttpStatus.CONFLICT,"Cette demande a déjà été utilisée pour une autre ligne. Actualisez la page.");
            return row;
        }
        var quote=quote(passage,r.serviceId(),r.performedOn(),r.quantity(),r.currency(),scope);
        var service=(Map<String,Object>)quote.get("service");var tariff=(Map<String,Object>)quote.get("tariff");
        if(!Objects.equals(r.tariffId(),id(tariff,"id")))throw error(HttpStatus.CONFLICT,"Le tarif applicable a changé. Vérifiez le nouveau montant avant d'enregistrer.");
        UUID id=UUID.randomUUID();
        db.update("""
            INSERT INTO billing_passage_charges(id,code,hospital_id,patient_id,patient_code,passage_id,passage_code,
                service_id,tariff_id,service_code,service_name,category,unit,performed_on,quantity,unit_price,currency,total_amount,request_id,note,created_by_id,created_by)
            VALUES(:id,:code,:hospital,:patient,:patientCode,:passage,:passageCode,:service,:tariff,:serviceCode,:name,:category,:unit,:date,:quantity,:price,:currency,:amount,:request,:note,:actor,:operator)
            """,p("id",id,"code",code("FRS-"),"hospital",passage.hospitalId(),"patient",passage.patientId(),"patientCode",passage.patientCode(),"passage",passageId,"passageCode",passage.code(),
                "service",r.serviceId(),"tariff",r.tariffId(),"serviceCode",service.get("code"),"name",service.get("name"),"category",service.get("category"),"unit",tariff.get("unit"),
                "date",r.performedOn(),"quantity",r.quantity(),"price",tariff.get("unitPrice"),"currency",r.currency(),"amount",quote.get("totalAmount"),"request",r.requestId(),"note",note,"actor",actor.userId(),"operator",actor.username()));
        return db.one("SELECT * FROM billing_passage_charges WHERE id=:id",p("id",id));
    }
    @Transactional
    public Map<String,Object> cancelCharge(UUID passageId,UUID chargeId,String reason,DataAccessScope scope,AuditActor actor){
        passage(passageId,scope);reason=required(reason,1000);
        var row=db.one("SELECT * FROM billing_passage_charges WHERE id=:id AND passage_id=:passage FOR UPDATE",p("id",chargeId,"passage",passageId));
        if("CANCELLED".equals(row.get("status")))return row;
        require("UNBILLED".equals(row.get("status")),"Seuls les frais non facturés peuvent être annulés.");
        db.update("UPDATE billing_passage_charges SET status='CANCELLED',cancelled_at=now(),cancelled_by_id=:actor,cancelled_by=:name,cancellation_reason=:reason WHERE id=:id",
            p("id",chargeId,"actor",actor.userId(),"name",actor.username(),"reason",reason));
        return db.one("SELECT * FROM billing_passage_charges WHERE id=:id",p("id",chargeId));
    }
}
