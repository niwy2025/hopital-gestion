package com.hopital.organization.patrimony;

import static com.hopital.organization.patrimony.PatrimonyRepository.id;
import static com.hopital.organization.patrimony.PatrimonyRepository.params;

import com.hopital.organization.application.dto.PageResponse;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly=true)
public class PatrimonyService {
    private final PatrimonyRepository db;
    private final PatrimonyClients clients;
    public PatrimonyService(PatrimonyRepository db, PatrimonyClients clients) { this.db=db; this.clients=clients; }

    public PageResponse<Map<String,Object>> search(String resource, int page, int size, String query, UUID hospitalId,
            String status, String category, UUID locationId, UUID assetId, PatrimonyScope scope) {
        if (Set.of("beds","locations").contains(resource)) { if (!scope.allows("read")) scope.require("beds"); } else scope.require("read");
        UUID hospital = scope.administrator() ? hospitalId : scope.hospital(null);
        page=Math.max(0,page); size=Math.max(1,Math.min(size,100));
        var p=params("hospital",hospital,"q","%"+(query==null?"":query.trim().toLowerCase(java.util.Locale.ROOT))+"%",
                "status",status==null?"":status,"category",category==null?"":category,"location",locationId,"asset",assetId,
                "limit",size,"offset",(long)page*size,"clinical",scope.allows("beds"));
        String from; String columns; String where;
        switch(resource) {
            case "locations" -> {
                from="patrimony_locations a JOIN hospitals h ON h.id=a.hospital_id LEFT JOIN patrimony_locations parent ON parent.id=a.parent_id";
                columns="a.*,h.name hospital_name,parent.name parent_name";
                where="(:status='' OR a.kind=:status) AND (LOWER(a.name) LIKE :q OR LOWER(a.code) LIKE :q)";
            }
            case "assets", "beds" -> {
                from="patrimony_assets a JOIN hospitals h ON h.id=a.hospital_id LEFT JOIN patrimony_locations l ON l.id=a.location_id LEFT JOIN patrimony_bed_stays b ON b.asset_id=a.id AND b.status<>'RELEASED'";
                columns="a.id,a.code,a.hospital_id,h.name hospital_name,a.name,a.category_code,a.status,a.location_id,l.name location_name,a.next_maintenance_on,a.cleaning_required,a.loan_due_on,a.responsible_name,b.status occupancy_status";
                if (resource.equals("beds")) columns+=",CASE WHEN :clinical THEN b.passage_code END passage_code,CASE WHEN :clinical THEN b.patient_code END patient_code,CASE WHEN :clinical THEN b.passage_id END passage_id";
                where="(:status='' OR a.status=:status OR b.status=:status OR (:status='FREE' AND b.id IS NULL AND a.status='AVAILABLE' AND NOT a.cleaning_required)) AND (:category='' OR a.category_code=:category) AND (CAST(:location AS uuid) IS NULL OR a.location_id=:location) AND (LOWER(a.name) LIKE :q OR LOWER(a.code) LIKE :q OR LOWER(COALESCE(a.serial_number,'')) LIKE :q)";
                if (resource.equals("beds")) where+=" AND a.category_code='BED'";
            }
            case "operations" -> {
                from="patrimony_operations o JOIN patrimony_assets a ON a.id=o.asset_id JOIN hospitals h ON h.id=a.hospital_id";
                columns="o.*,a.name asset_name,a.code asset_code,a.hospital_id,h.name hospital_name";
                where="(:status='' OR o.status=:status) AND (CAST(:asset AS uuid) IS NULL OR a.id=:asset) AND (LOWER(a.name) LIKE :q OR LOWER(a.code) LIKE :q OR LOWER(o.code) LIKE :q)";
            }
            case "events" -> {
                from="patrimony_events o LEFT JOIN patrimony_assets a ON a.id=o.asset_id JOIN hospitals h ON h.id=o.hospital_id";
                columns="o.id,o.asset_id,o.location_id,o.kind,o.note,o.operator_name,o.created_at,a.code asset_code,a.name asset_name,h.name hospital_name";
                where="(:status='' OR o.kind=:status) AND (CAST(:asset AS uuid) IS NULL OR a.id=:asset) AND (LOWER(o.note) LIKE :q OR LOWER(COALESCE(a.code,'')) LIKE :q)";
            }
            case "documents" -> {
                from="patrimony_documents o JOIN patrimony_assets a ON a.id=o.asset_id JOIN hospitals h ON h.id=a.hospital_id";
                columns="o.id,o.asset_id,o.kind,o.file_name,o.content_type,o.size_bytes,o.created_at,o.created_by,a.name asset_name";
                where="(CAST(:asset AS uuid) IS NULL OR a.id=:asset) AND LOWER(o.file_name) LIKE :q";
            }
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String owner=resource.equals("events")?"o.hospital_id":"a.hospital_id";
        String filter=" WHERE (CAST(:hospital AS uuid) IS NULL OR "+owner+"=:hospital) AND "+where;
        String sort=Set.of("events","operations","documents").contains(resource)?"o."+(resource.equals("operations")?"requested_at":"created_at")+" DESC,o.id":"a.name,a.id";
        long total=db.count("SELECT count(*) FROM "+from+filter,p);
        return new PageResponse<>(db.list("SELECT "+columns+" FROM "+from+filter+" ORDER BY "+sort+" LIMIT :limit OFFSET :offset",p),page,size,total,(int)Math.ceil((double)total/size));
    }

    public List<Map<String,Object>> categories(PatrimonyScope scope) {
        if (!scope.allows("read")) scope.require("beds");
        return db.list("SELECT * FROM patrimony_categories WHERE active ORDER BY name",params());
    }

    @Transactional
    public Map<String,Object> createLocation(PatrimonyRequests.Location r, PatrimonyScope s) {
        s.require("write"); UUID hospital=s.hospital(r.hospitalId()); activeHospital(hospital);
        requireValue(r.kind(),Set.of("BUILDING","FLOOR","ROOM"));
        if (r.parentId()!=null) {
            var parent=location(r.parentId(),hospital);
            if ("ROOM".equals(parent.get("kind")) || "BUILDING".equals(r.kind()) || ("FLOOR".equals(r.kind()) && !"BUILDING".equals(parent.get("kind")))) conflict("La hiérarchie attendue est bâtiment, étage facultatif, puis salle.");
        } else if (!"BUILDING".equals(r.kind())) conflict("Choisissez le bâtiment ou l’étage parent.");
        UUID key=UUID.randomUUID();
        db.update("INSERT INTO patrimony_locations(id,code,hospital_id,parent_id,kind,name,service_name,created_by) VALUES(:id,:code,:hospital,:parent,:kind,:name,:service,:by)",
                params("id",key,"code",code("LOC"),"hospital",hospital,"parent",r.parentId(),"kind",r.kind(),"name",r.name().trim(),"service",clean(r.serviceName()),"by",s.username()));
        event(null,key,hospital,"LOCATION_CREATED",r.name(),s);
        return db.one("SELECT * FROM patrimony_locations WHERE id=:id",params("id",key));
    }

    @Transactional
    public void locationStatus(UUID key, boolean active, PatrimonyScope s) {
        s.require("write"); var l=db.one("SELECT * FROM patrimony_locations WHERE id=:id FOR UPDATE",params("id",key)); s.checkHospital(id(l,"hospitalId"));
        if (!active && db.count("SELECT (SELECT count(*) FROM patrimony_locations WHERE parent_id=:id AND active)+(SELECT count(*) FROM patrimony_assets WHERE location_id=:id AND status NOT IN ('RETIRED','LOST'))",params("id",key))>0)
            conflict("Déplacez les équipements et désactivez les locaux enfants avant de fermer ce local.");
        if (active && id(l,"parentId")!=null) location(id(l,"parentId"),id(l,"hospitalId"));
        db.update("UPDATE patrimony_locations SET active=:active WHERE id=:id",params("id",key,"active",active));
        event(null,key,id(l,"hospitalId"),"LOCATION_STATUS",active?"Local réactivé":"Local désactivé",s);
    }

    public Map<String,Object> asset(UUID key, PatrimonyScope s) {
        s.require("read"); var a=assetRow(key,false); s.checkHospital(id(a,"hospitalId"));
        a.put("canWrite",s.allows("write")); a.put("canMaintain",s.allows("maintain")); a.put("canValidate",s.allows("validate"));
        a.put("accountingStatus", db.one("SELECT COALESCE((SELECT status FROM patrimony_accounting_outbox WHERE asset_id=:id),'PENDING') AS status",params("id",key)).get("status"));
        return a;
    }

    @Transactional
    public Map<String,Object> saveAsset(UUID key, PatrimonyRequests.Asset r, PatrimonyScope s) {
        s.require("write"); UUID hospital=s.hospital(r.hospitalId()); activeHospital(hospital);
        requireValue(r.acquisitionSource(),Set.of("PURCHASE","DONATION","PROVISION","EXISTING")); requireValue(r.currency(),Set.of("CDF","USD"));
        if (r.receivedOn().isAfter(LocalDate.now())) conflict("La date de réception ne peut pas être dans le futur.");
        if (db.count("SELECT count(*) FROM patrimony_categories WHERE code=:code AND active",params("code",r.categoryCode()))!=1) conflict("Catégorie inconnue.");
        if (r.locationId()!=null) location(r.locationId(),hospital);
        boolean create=key==null;
        if (create) key=UUID.randomUUID();
        else {
            var a=assetRow(key,true); s.checkHospital(id(a,"hospitalId"));
            if (!hospital.equals(id(a,"hospitalId"))) conflict("L’établissement propriétaire ne peut pas être remplacé.");
            if (r.version()==null || !r.version().equals(a.get("version"))) conflict("Cette fiche a été modifiée. Rechargez-la.");
            if (!Objects.equals(r.locationId(),id(a,"locationId"))) conflict("Utilisez un mouvement pour changer l’emplacement.");
            if (!r.categoryCode().equals(a.get("categoryCode"))) conflict("La catégorie n’est pas modifiable après enregistrement.");
            if (Set.of("RETIRED","LOST").contains(a.get("status"))) conflict("Un bien sorti du patrimoine ne peut plus être modifié.");
        }
        var p=params("id",key,"code",code("INV"),"hospital",hospital,"category",r.categoryCode(),"name",r.name().trim(),"brand",clean(r.brand()),
                "model",clean(r.model()),"serial",clean(r.serialNumber()),"location",r.locationId(),"source",r.acquisitionSource(),"owner",clean(r.ownerName()),
                "supplier",clean(r.supplierName()),"received",r.receivedOn(),"commissioned",r.commissionedOn(),"cost",r.purchaseCost(),"currency",r.currency(),
                "warranty",r.warrantyUntil(),"maintenance",r.nextMaintenanceOn(),"notes",clean(r.notes()),"by",s.username(),"actor",s.userId());
        if (create) db.update("""
                INSERT INTO patrimony_assets(id,code,hospital_id,category_code,name,brand,model,serial_number,location_id,acquisition_source,owner_name,supplier_name,received_on,commissioned_on,purchase_cost,currency,warranty_until,next_maintenance_on,notes,created_by_id,created_by)
                VALUES(:id,:code,:hospital,:category,:name,:brand,:model,:serial,:location,:source,:owner,:supplier,:received,:commissioned,:cost,:currency,:warranty,:maintenance,:notes,:actor,:by)
                """,p);
        else db.update("""
                UPDATE patrimony_assets SET name=:name,brand=:brand,model=:model,serial_number=:serial,acquisition_source=:source,owner_name=:owner,
                supplier_name=:supplier,received_on=:received,commissioned_on=:commissioned,purchase_cost=:cost,currency=:currency,
                warranty_until=:warranty,next_maintenance_on=:maintenance,notes=:notes,version=version+1,updated_at=now() WHERE id=:id
                """,p);
        event(key,null,hospital,create?"RECEIVED":"UPDATED",create?"Bien enregistré : "+r.name():"Fiche du bien mise à jour",s); enqueue(key);
        return asset(key,s);
    }

    @Transactional
    public void assign(UUID key, PatrimonyRequests.Assignment r, PatrimonyScope s) {
        s.require("write"); var a=assetRow(key,true); s.checkHospital(id(a,"hospitalId"));
        if (Set.of("RETIRED","LOST").contains(a.get("status"))) conflict("Ce bien est sorti du patrimoine.");
        String name=null;
        if (r.personnelId()!=null) {
            var person=clients.personnel(r.personnelId(),id(a,"hospitalId"));
            name=java.util.stream.Stream.of(person.lastName(),person.middleName(),person.firstName()).filter(Objects::nonNull).collect(java.util.stream.Collectors.joining(" "));
        }
        db.update("UPDATE patrimony_assets SET responsible_personnel_id=:person,responsible_name=:name,version=version+1,updated_at=now() WHERE id=:id",params("id",key,"person",r.personnelId(),"name",name));
        event(key,null,id(a,"hospitalId"),"ASSIGNED",(name==null?"Responsable retiré":name)+" — "+r.note(),s);
    }

    @Transactional
    public void move(UUID key, PatrimonyRequests.Movement r, PatrimonyScope s) {
        s.require("write"); var a=assetRow(key,true); s.checkHospital(id(a,"hospitalId"));
        requireValue(r.kind(),Set.of("MOVE","LOAN","RETURN","INVENTORY_FOUND","INVENTORY_MISSING","FAULT","CLEANED"));
        if (Set.of("RETIRED","LOST").contains(a.get("status"))) conflict("Ce bien est sorti du patrimoine.");
        if (r.kind().startsWith("INVENTORY_")) { event(key,null,id(a,"hospitalId"),r.kind(),r.note(),s); return; }
        if (!r.kind().equals("FAULT")) requireNoOccupant(key);
        String next=a.get("status").toString(); UUID target=id(a,"locationId");
        switch(r.kind()) {
            case "MOVE" -> { if (Set.of("LOANED","MAINTENANCE").contains(next)) conflict("Récupérez le bien ou terminez l’intervention avant de le déplacer."); if(r.locationId()==null) conflict("Choisissez un local."); location(r.locationId(),id(a,"hospitalId")); target=r.locationId(); }
            case "LOAN" -> { if (!next.equals("AVAILABLE") || r.recipient()==null || r.recipient().isBlank() || r.dueOn()==null || r.dueOn().isBefore(LocalDate.now())) conflict("Précisez le bénéficiaire et une date de retour pour un bien disponible."); next="LOANED"; }
            case "RETURN" -> { if (!next.equals("LOANED")) conflict("Ce bien n’est pas en prêt."); next="AVAILABLE"; }
            case "FAULT" -> { if (!next.equals("AVAILABLE")) conflict("Ce bien n’est pas en service."); next="FAULTY"; }
            case "CLEANED" -> { if (!"BED".equals(a.get("categoryCode")) || !"AVAILABLE".equals(next)) conflict("Le nettoyage concerne un lit présent et en état de service."); }
            default -> { }
        }
        var p=params("id",key,"location",target,"status",next,"recipient",r.kind().equals("LOAN")?r.recipient():null,"due",r.kind().equals("LOAN")?r.dueOn():null);
        db.update("UPDATE patrimony_assets SET location_id=:location,status=:status,loan_recipient=:recipient,loan_due_on=:due,version=version+1,updated_at=now() WHERE id=:id",p);
        if (r.kind().equals("CLEANED")) db.update("UPDATE patrimony_assets SET cleaning_required=false WHERE id=:id",p);
        event(key,null,id(a,"hospitalId"),r.kind(),r.note()+(target==null?"":" — Local "+target),s);
        enqueue(key);
    }

    @Transactional
    public Map<String,Object> createOperation(UUID key, PatrimonyRequests.Operation r, PatrimonyScope s) {
        if ("MAINTENANCE".equals(r.kind())) s.require("maintain"); else s.require("write");
        requireValue(r.kind(),Set.of("MAINTENANCE","RETIREMENT","LOSS")); var a=assetRow(key,true); s.checkHospital(id(a,"hospitalId"));
        if (Set.of("RETIRED","LOST","LOANED").contains(a.get("status"))) conflict("Ce bien doit être présent dans le patrimoine pour ouvrir cette opération.");
        if (db.count("SELECT count(*) FROM patrimony_operations WHERE asset_id=:id AND status IN ('REQUESTED','IN_PROGRESS')",params("id",key))>0) conflict("Une opération est déjà ouverte.");
        UUID operation=UUID.randomUUID();
        db.update("INSERT INTO patrimony_operations(id,code,asset_id,kind,reason,planned_on,requested_by_id,requested_by) VALUES(:id,:code,:asset,:kind,:reason,:planned,:actor,:by)",
                params("id",operation,"code",code("OPR"),"asset",key,"kind",r.kind(),"reason",r.reason(),"planned",r.plannedOn(),"actor",s.userId(),"by",s.username()));
        event(key,null,id(a,"hospitalId"),"OPERATION_REQUESTED",r.kind()+" — "+r.reason(),s);
        return db.one("SELECT * FROM patrimony_operations WHERE id=:id",params("id",operation));
    }

    @Transactional
    public void completeOperation(UUID operation, PatrimonyRequests.CompleteOperation r, PatrimonyScope s) {
        var initial=db.one("SELECT asset_id FROM patrimony_operations WHERE id=:id",params("id",operation));
        var a=assetRow(id(initial,"assetId"),true); s.checkHospital(id(a,"hospitalId"));
        var op=db.one("SELECT * FROM patrimony_operations WHERE id=:id FOR UPDATE",params("id",operation));
        boolean maintenance="MAINTENANCE".equals(op.get("kind")); s.require(maintenance?"maintain":"validate");
        requireValue(r.decision(),Set.of("START","COMPLETE","REJECT"));
        if (Set.of("COMPLETED","REJECTED").contains(op.get("status"))) conflict("Cette opération est déjà terminée.");
        if (!maintenance && s.userId().equals(op.get("requestedById"))) conflict("La sortie doit être validée par un autre administrateur.");
        if (!maintenance && r.decision().equals("START")) conflict("Validez ou refusez la sortie.");
        if (r.decision().equals("START") && !"REQUESTED".equals(op.get("status"))) conflict("Cette intervention a déjà démarré.");
        if (maintenance && r.decision().equals("COMPLETE") && !"IN_PROGRESS".equals(op.get("status"))) conflict("Démarrez d’abord l’intervention.");
        if (r.decision().equals("REJECT") && "IN_PROGRESS".equals(op.get("status"))) conflict("Terminez l’intervention en cours avec un compte rendu.");
        if (!r.decision().equals("REJECT")) requireNoOccupant(id(a,"id"));
        String state=r.decision().equals("START")?"IN_PROGRESS":r.decision().equals("REJECT")?"REJECTED":"COMPLETED";
        db.update("UPDATE patrimony_operations SET status=:status,result_note=:note,cost=:cost,completed_by=:by,completed_at=CASE WHEN :status='IN_PROGRESS' THEN NULL ELSE now() END WHERE id=:id",
                params("id",operation,"status",state,"note",r.note(),"cost",r.cost(),"by",s.username()));
        if (!state.equals("REJECTED")) {
            String assetStatus=maintenance?(state.equals("IN_PROGRESS")?"MAINTENANCE":r.operational()?"AVAILABLE":"FAULTY"):op.get("kind").equals("LOSS")?"LOST":"RETIRED";
            db.update("UPDATE patrimony_assets SET status=:status,version=version+1,updated_at=now(),next_maintenance_on=COALESCE(:next,next_maintenance_on) WHERE id=:id",
                    params("id",id(a,"id"),"status",assetStatus,"next",r.nextMaintenanceOn()));
            enqueue(id(a,"id"));
        }
        event(id(a,"id"),null,id(a,"hospitalId"),"OPERATION_"+state,op.get("code")+" — "+r.note(),s);
    }

    @Transactional
    public Map<String,Object> addDocument(UUID key, PatrimonyRequests.Document r, PatrimonyScope s) {
        s.require("maintain"); var a=assetRow(key,false); s.checkHospital(id(a,"hospitalId"));
        requireValue(r.kind(),Set.of("PHOTO","INVOICE","MANUAL","WARRANTY","REPORT","OTHER"));
        requireValue(r.contentType(),Set.of("image/jpeg","image/png","image/webp","application/pdf"));
        byte[] bytes;
        try { bytes=Base64.getDecoder().decode(r.base64()); } catch(IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Fichier invalide."); }
        if (bytes.length==0 || bytes.length>3145728) conflict("Le fichier doit peser au maximum 3 Mo.");
        boolean valid=switch(r.contentType()) {
            case "application/pdf" -> starts(bytes,new byte[]{37,80,68,70,45});
            case "image/jpeg" -> starts(bytes,new byte[]{(byte)255,(byte)216,(byte)255});
            case "image/png" -> starts(bytes,new byte[]{(byte)137,80,78,71,13,10,26,10});
            default -> bytes.length>12 && starts(bytes,new byte[]{82,73,70,70}) && bytes[8]==87 && bytes[9]==69 && bytes[10]==66 && bytes[11]==80;
        };
        if (!valid) conflict("Le contenu du fichier ne correspond pas au format annoncé.");
        UUID document=UUID.randomUUID();
        db.update("INSERT INTO patrimony_documents(id,asset_id,kind,file_name,content_type,content,size_bytes,created_by) VALUES(:id,:asset,:kind,:name,:type,:bytes,:size,:by)",
                params("id",document,"asset",key,"kind",r.kind(),"name",r.fileName().replaceAll("[\\\\/\\r\\n]","_"),"type",r.contentType(),"bytes",bytes,"size",bytes.length,"by",s.username()));
        event(key,null,id(a,"hospitalId"),"DOCUMENT_ADDED",r.kind()+" — "+r.fileName(),s);
        return Map.of("id",document);
    }
    public Map<String,Object> document(UUID key, PatrimonyScope s) {
        s.require("read"); var d=db.one("SELECT d.*,a.hospital_id FROM patrimony_documents d JOIN patrimony_assets a ON a.id=d.asset_id WHERE d.id=:id",params("id",key));
        s.checkHospital(id(d,"hospitalId")); d.put("base64",Base64.getEncoder().encodeToString((byte[])d.remove("content"))); return d;
    }

    @Transactional
    public Map<String,Object> occupy(UUID key, PatrimonyRequests.Bed r, PatrimonyScope s) {
        s.require("beds"); var a=assetRow(key,true); s.checkHospital(id(a,"hospitalId"));
        if (!"BED".equals(a.get("categoryCode")) || !"AVAILABLE".equals(a.get("status")) || Boolean.TRUE.equals(a.get("cleaningRequired"))) conflict("Ce lit n’est pas disponible pour un patient.");
        if (id(a,"locationId")==null || !"ROOM".equals(location(id(a,"locationId"),id(a,"hospitalId")).get("kind"))) conflict("Affectez d’abord ce lit à une salle active.");
        var passage=clients.passage(r.passageId());
        if (!id(a,"hospitalId").equals(passage.hospitalId()) || !"OPEN".equals(passage.status())) conflict("Choisissez un passage en cours dans cet hôpital.");
        var existing=db.list("SELECT * FROM patrimony_bed_stays WHERE asset_id=:id AND status<>'RELEASED'",params("id",key));
        if (!existing.isEmpty()) {
            if (r.passageId().equals(id(existing.get(0),"passageId"))) {
                if (!r.reserved() && "RESERVED".equals(existing.get(0).get("status"))) {
                    db.update("UPDATE patrimony_bed_stays SET status='OCCUPIED' WHERE id=:id",params("id",id(existing.get(0),"id")));
                    existing.get(0).put("status","OCCUPIED");
                    event(key,null,id(a,"hospitalId"),"BED_ASSIGNED","Réservation confirmée : lit occupé",s);
                }
                return existing.get(0);
            }
            conflict("Ce lit est déjà réservé ou occupé.");
        }
        UUID stay=UUID.randomUUID();
        db.update("INSERT INTO patrimony_bed_stays(id,asset_id,passage_id,passage_code,patient_code,status,started_by,note) VALUES(:id,:asset,:passage,:code,:patient,:status,:by,:note)",
                params("id",stay,"asset",key,"passage",r.passageId(),"code",passage.passageCode(),"patient",passage.patientCode(),"status",r.reserved()?"RESERVED":"OCCUPIED","by",s.username(),"note",r.note()));
        event(key,null,id(a,"hospitalId"),"BED_ASSIGNED","Lit "+(r.reserved()?"réservé":"occupé"),s);
        return Map.of("id",stay);
    }
    public Map<String,Object> bed(UUID key, PatrimonyScope s) {
        if (!s.allows("read")) s.require("beds");
        var a=assetRow(key,false); s.checkHospital(id(a,"hospitalId"));
        if (!"BED".equals(a.get("categoryCode"))) conflict("Cet équipement n’est pas un lit.");
        a.keySet().retainAll(Set.of("id","code","name","hospitalId","hospitalName","locationName","status","cleaningRequired"));
        if (s.allows("beds")) {
            var stay=db.list("SELECT passage_id,passage_code FROM patrimony_bed_stays WHERE asset_id=:id AND status<>'RELEASED'",params("id",key));
            if (!stay.isEmpty()) a.putAll(stay.get(0));
        }
        return a;
    }
    @Transactional
    public void release(UUID key, String note, PatrimonyScope s) {
        s.require("beds"); var a=assetRow(key,true); s.checkHospital(id(a,"hospitalId")); releaseLocked(a,note,s);
    }
    public void releaseLocked(Map<String,Object> a, String note, PatrimonyScope s) {
        UUID key=id(a,"id");
        int updated=db.update("UPDATE patrimony_bed_stays SET status='RELEASED',ended_at=now(),ended_by=:by,note=:note WHERE asset_id=:id AND status<>'RELEASED'",params("id",key,"by",s.username(),"note",note));
        if(updated>0) {
            db.update("UPDATE patrimony_assets SET cleaning_required=true,version=version+1 WHERE id=:id",params("id",key));
            event(key,null,id(a,"hospitalId"),"BED_RELEASED","Lit libéré ; nettoyage requis",s);
        }
    }
    public Map<String,Object> accountingReference(UUID key) {
        var a=assetRow(key,false);
        a.keySet().retainAll(Set.of("id","code","name","hospitalId","hospitalName","categoryCode","purchaseCost","currency","receivedOn","commissionedOn","acquisitionSource","supplierName","ownerName","status","version"));
        return a;
    }
    private Map<String,Object> assetRow(UUID key, boolean lock) {
        return db.one("SELECT a.*,h.name hospital_name,l.name location_name FROM patrimony_assets a JOIN hospitals h ON h.id=a.hospital_id LEFT JOIN patrimony_locations l ON l.id=a.location_id WHERE a.id=:id"+(lock?" FOR UPDATE OF a":""),params("id",key));
    }
    private Map<String,Object> location(UUID key, UUID hospital) {
        var row=db.one("SELECT * FROM patrimony_locations WHERE id=:id FOR SHARE",params("id",key));
        if (!hospital.equals(id(row,"hospitalId")) || !Boolean.TRUE.equals(row.get("active"))) conflict("Choisissez un local actif de cet hôpital.");
        return row;
    }
    private void activeHospital(UUID hospital) {
        if (db.count("SELECT count(*) FROM hospitals WHERE id=:id AND active",params("id",hospital))!=1) conflict("Choisissez un hôpital actif.");
    }
    private void requireNoOccupant(UUID key) {
        if (db.count("SELECT count(*) FROM patrimony_bed_stays WHERE asset_id=:id AND status<>'RELEASED'",params("id",key))>0) conflict("Libérez d’abord le lit avec l’équipe de soins.");
    }
    private void event(UUID asset, UUID location, UUID hospital, String kind, String note, PatrimonyScope s) {
        // L’événement est un résumé ; les comptes rendus complets restent sur l’opération.
        if (note.length()>4000) note=note.substring(0,3999)+"…";
        db.update("INSERT INTO patrimony_events(id,asset_id,location_id,hospital_id,kind,note,operator_id,operator_name) VALUES(:id,:asset,:location,:hospital,:kind,:note,:actor,:by)",params("id",UUID.randomUUID(),"asset",asset,"location",location,"hospital",hospital,"kind",kind,"note",note,"actor",s.userId(),"by",s.username()));
    }
    private void enqueue(UUID key) {
        db.update("INSERT INTO patrimony_accounting_outbox(id,asset_id) VALUES(:id,:asset) ON CONFLICT(asset_id) DO UPDATE SET status='PENDING',retry_at=now()",params("id",UUID.randomUUID(),"asset",key));
    }
    private static boolean starts(byte[] bytes,byte[] prefix) { if(bytes.length<prefix.length)return false; for(int i=0;i<prefix.length;i++)if(bytes[i]!=prefix[i])return false; return true; }
    private static String code(String prefix) { return prefix+"-"+UUID.randomUUID().toString().replace("-","").substring(0,20).toUpperCase(java.util.Locale.ROOT); }
    private static String clean(String value) { return value==null||value.isBlank()?null:value.trim(); }
    private static void requireValue(String value,Set<String> allowed) { if(!allowed.contains(value)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Valeur inconnue : "+value); }
    private static void conflict(String reason) { throw new ResponseStatusException(HttpStatus.CONFLICT,reason); }
}
