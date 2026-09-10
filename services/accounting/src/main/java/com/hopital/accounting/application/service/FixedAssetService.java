package com.hopital.accounting.application.service;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AuditActor;
import com.hopital.accounting.application.domain.DataAccessScope;
import com.hopital.accounting.application.dto.CreateAccountingEntryLineRequest;
import com.hopital.accounting.application.dto.CreateAccountingEntryRequest;
import com.hopital.accounting.application.dto.PageResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly=true)
public class FixedAssetService {
    private final NamedParameterJdbcTemplate db; private final RestClient organization; private final AccountingApplicationService accounting;
    public FixedAssetService(NamedParameterJdbcTemplate db,RestClient.Builder builder,AccountingApplicationService accounting,
            @Value("${hospital.organization-service.base-url}") String url) {
        this.db=db; this.accounting=accounting;
        var factory=new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000); factory.setReadTimeout(5000);
        this.organization=builder.clone().baseUrl(url).requestFactory(factory).build();
    }
    public record Source(UUID id,UUID hospitalId,String code,String name,BigDecimal purchaseCost,String currency,LocalDate receivedOn,String status,int version) { }
    public record Classification(@NotBlank String classification,@DecimalMin("0") BigDecimal accountingValue,@DecimalMin("0") BigDecimal residualValue,
            LocalDate startOn,@Min(1) @Max(1200) Integer usefulLifeMonths,UUID assetAccountId,UUID depreciationAccountId,UUID expenseAccountId,
            boolean depreciable,@NotBlank @Size(max=4000) String note,@NotNull Integer version) { }
    public record Installment(@NotNull UUID journalId,@Min(1) int installment) { }

    @Transactional
    public void importAsset(UUID key) {
        Source source=organization.get().uri("/internal/organizations/patrimony/assets/{id}/accounting-reference",key).retrieve().body(Source.class);
        if(source==null || !key.equals(source.id())) throw new IllegalStateException("Référence du bien indisponible");
        db.update("""
                INSERT INTO accounting_fixed_assets(id,hospital_id,asset_code,name,source_cost,source_currency,currency,received_on,asset_status,source_version)
                VALUES(:id,:hospital,:code,:name,:cost,:currency,:currency,:received,:status,:version)
                ON CONFLICT(id) DO UPDATE SET name=EXCLUDED.name,source_cost=EXCLUDED.source_cost,source_currency=EXCLUDED.source_currency,
                currency=CASE WHEN accounting_fixed_assets.classification='PENDING' THEN EXCLUDED.currency ELSE accounting_fixed_assets.currency END,
                asset_status=EXCLUDED.asset_status,received_on=EXCLUDED.received_on,source_version=EXCLUDED.source_version,source_updated_at=now(),
                version=accounting_fixed_assets.version+1
                WHERE accounting_fixed_assets.source_version<EXCLUDED.source_version
                """,p("id",source.id(),"hospital",source.hospitalId(),"code",source.code(),"name",source.name(),"cost",source.purchaseCost(),"currency",source.currency(),"received",source.receivedOn(),"status",source.status(),"version",source.version()));
    }
    public PageResponse<Map<String,Object>> search(int page,int size,String query,String status,UUID hospital,DataAccessScope scope) {
        hospital=scope.administrator()?hospital:scope.hospitalId();
        if(!scope.administrator()&&hospital==null) deny(); page=Math.max(0,page);size=Math.max(1,Math.min(size,100));
        var params=p("hospital",hospital,"q","%"+(query==null?"":query.toLowerCase(java.util.Locale.ROOT).trim())+"%","status",status==null?"":status,"limit",size,"offset",(long)page*size);
        String where=" FROM accounting_fixed_assets WHERE (CAST(:hospital AS uuid) IS NULL OR hospital_id=:hospital) AND (:status='' OR classification=:status) AND (LOWER(name) LIKE :q OR LOWER(asset_code) LIKE :q)";
        long total=db.queryForObject("SELECT count(*)"+where,params,Long.class);
        return new PageResponse<>(rows("SELECT id,asset_code,name,hospital_id,classification,source_cost,source_currency,currency,accounting_value,asset_status"+where+" ORDER BY name,id LIMIT :limit OFFSET :offset",params),page,size,total,(int)Math.ceil((double)total/size));
    }
    public Map<String,Object> get(UUID key,DataAccessScope scope) { return owned(key,scope,false); }
    @Transactional
    public Map<String,Object> classify(UUID key,Classification r,DataAccessScope scope,AuditActor actor) {
        var a=owned(key,scope,true); UUID hospital=uuid(a,"hospitalId");
        if(!r.version().equals(a.get("version"))) conflict("La fiche comptable a été modifiée. Rechargez-la.");
        if(!Set.of("EXPENSE","IMMOBILIZATION","THIRD_PARTY").contains(r.classification())) conflict("Choisissez le traitement du bien.");
        if(db.queryForObject("SELECT count(*) FROM accounting_fixed_asset_installments WHERE asset_id=:id",p("id",key),Long.class)>0) conflict("Le plan comporte déjà des écritures. Sa révision exige un traitement comptable distinct.");
        if(r.accountingValue()==null || r.residualValue()==null || r.residualValue().compareTo(r.accountingValue())>0) conflict("Vérifiez la valeur comptable et la valeur résiduelle.");
        boolean capital="IMMOBILIZATION".equals(r.classification());
        if(capital) {
            account(r.assetAccountId(),hospital,"2");
            if(db.queryForObject("SELECT count(*) FROM accounting_accounts WHERE id=:id AND (account_number LIKE '28%' OR account_number LIKE '29%')",p("id",r.assetAccountId()),Long.class)>0)
                conflict("Choisissez un compte d’immobilisation, hors amortissements et dépréciations.");
        }
        if(r.depreciable()) {
            if(!capital || r.startOn()==null || r.usefulLifeMonths()==null || r.accountingValue().compareTo(r.residualValue())<=0) conflict("Complétez le plan d’amortissement.");
            account(r.depreciationAccountId(),hospital,"28"); account(r.expenseAccountId(),hospital,"68");
        }
        var params=p("id",key,"classification",r.classification(),"value",r.accountingValue(),"residual",r.residualValue(),"start",r.startOn(),"months",r.usefulLifeMonths(),
                "assetAccount",capital?r.assetAccountId():null,"depreciationAccount",r.depreciable()?r.depreciationAccountId():null,"expenseAccount",r.depreciable()?r.expenseAccountId():null,"depreciable",r.depreciable(),"note",r.note(),"by",actor.username(),"actor",actor.userId(),"review",UUID.randomUUID());
        db.update("""
                UPDATE accounting_fixed_assets SET classification=:classification,accounting_value=:value,residual_value=:residual,start_on=:start,
                useful_life_months=:months,asset_account_id=:assetAccount,depreciation_account_id=:depreciationAccount,expense_account_id=:expenseAccount,
                depreciable=:depreciable,currency=source_currency,review_note=:note,reviewed_by=:by,reviewed_at=now(),version=version+1 WHERE id=:id
                """,params);
        db.update("""
                INSERT INTO accounting_fixed_asset_reviews(id,asset_id,classification,accounting_value,residual_value,start_on,useful_life_months,asset_account_id,
                depreciation_account_id,expense_account_id,depreciable,note,operator_id,operator_name)
                VALUES(:review,:id,:classification,:value,:residual,:start,:months,:assetAccount,:depreciationAccount,:expenseAccount,:depreciable,:note,:actor,:by)
                """,params);
        return owned(key,scope,false);
    }
    public PageResponse<Map<String,Object>> schedule(UUID key,int page,int size,DataAccessScope scope) {
        var a=owned(key,scope,false);page=Math.max(0,page);size=Math.max(1,Math.min(size,100));
        if(!Boolean.TRUE.equals(a.get("depreciable"))) return new PageResponse<>(List.of(),page,size,0,0);
        int months=((Number)a.get("usefulLifeMonths")).intValue(); var result=new ArrayList<Map<String,Object>>();
        LocalDate start=LocalDate.parse(a.get("startOn").toString()).withDayOfMonth(1);
        for(int i=(int)Math.min((long)page*size,months);i<Math.min((long)(page+1)*size,months);i++) {
            int n=i+1; var row=new LinkedHashMap<String,Object>(); row.put("installment",n);row.put("date",start.plusMonths(n-1).withDayOfMonth(start.plusMonths(n-1).lengthOfMonth()).toString());row.put("amount",amount(a,n));
            var entries=rows("SELECT i.entry_id,e.code,e.status FROM accounting_fixed_asset_installments i JOIN accounting_entries e ON e.id=i.entry_id WHERE i.asset_id=:id AND i.installment=:n",p("id",key,"n",n));
            if(!entries.isEmpty())row.putAll(entries.get(0)); result.add(row);
        }
        return new PageResponse<>(result,page,size,months,(int)Math.ceil((double)months/size));
    }
    @Transactional
    public Map<String,Object> prepareInstallment(UUID key,Installment r,DataAccessScope scope,AuditActor actor) {
        var a=owned(key,scope,true);
        var existing=rows("SELECT entry_id FROM accounting_fixed_asset_installments WHERE asset_id=:id AND installment=:n",p("id",key,"n",r.installment()));
        if(!existing.isEmpty())return existing.get(0);
        // Relecture de la source avant un nouveau brouillon : une sortie peut encore être en file.
        importAsset(key);
        a=owned(key,scope,false);
        if(!a.get("currency").equals(a.get("sourceCurrency"))) conflict("La devise du bien a changé. Faites vérifier le plan comptable avant de poursuivre.");
        if(!Boolean.TRUE.equals(a.get("depreciable")) || r.installment()>((Number)a.get("usefulLifeMonths")).intValue()) conflict("Échéance non prévue au plan.");
        if(Set.of("RETIRED","LOST").contains(a.get("assetStatus"))) conflict("Ce bien est sorti du patrimoine. Vérifiez son traitement comptable.");
        LocalDate date=LocalDate.parse(a.get("startOn").toString()).withDayOfMonth(1).plusMonths(r.installment()-1); date=date.withDayOfMonth(date.lengthOfMonth());
        if(date.isAfter(LocalDate.now())) conflict("Cette échéance n’est pas encore atteinte.");
        BigDecimal amount=amount(a,r.installment());
        if(amount.signum()<=0)conflict("L’échéance est inférieure à la précision monétaire.");
        String label="Amortissement "+a.get("assetCode")+" — échéance "+r.installment();
        var entry=accounting.createManualEntry(new CreateAccountingEntryRequest(uuid(a,"hospitalId"),r.journalId(),date,label,AccountingCurrency.valueOf(a.get("currency").toString()),List.of(
                new CreateAccountingEntryLineRequest(uuid(a,"expenseAccountId"),label,amount,BigDecimal.ZERO,a.get("assetCode").toString()),
                new CreateAccountingEntryLineRequest(uuid(a,"depreciationAccountId"),label,BigDecimal.ZERO,amount,a.get("assetCode").toString()))),scope,actor);
        db.update("INSERT INTO accounting_fixed_asset_installments(id,asset_id,installment,entry_id) VALUES(:id,:asset,:n,:entry)",p("id",UUID.randomUUID(),"asset",key,"n",r.installment(),"entry",entry.id()));
        return Map.of("entryId",entry.id());
    }
    static BigDecimal amount(Map<String,Object> a,int installment) {
        BigDecimal base=((BigDecimal)a.get("accountingValue")).subtract((BigDecimal)a.get("residualValue"));
        int months=((Number)a.get("usefulLifeMonths")).intValue();
        BigDecimal through=base.multiply(BigDecimal.valueOf(installment)).divide(BigDecimal.valueOf(months),2,RoundingMode.HALF_UP);
        BigDecimal previous=base.multiply(BigDecimal.valueOf(installment-1)).divide(BigDecimal.valueOf(months),2,RoundingMode.HALF_UP);
        return through.subtract(previous);
    }
    private void account(UUID id,UUID hospital,String prefix) {
        if(id==null || db.queryForObject("SELECT count(*) FROM accounting_accounts WHERE id=:id AND hospital_id=:hospital AND active AND account_number LIKE :prefix",p("id",id,"hospital",hospital,"prefix",prefix+"%"),Long.class)!=1)
            conflict("Choisissez un compte actif de l’hôpital dans la classe "+prefix+".");
    }
    private Map<String,Object> owned(UUID key,DataAccessScope scope,boolean lock) {
        var rows=rows("SELECT * FROM accounting_fixed_assets WHERE id=:id"+(lock?" FOR UPDATE":""),p("id",key));
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Bien non encore transmis à la comptabilité.");
        var a=rows.get(0);if(!scope.administrator() && !java.util.Objects.equals(scope.hospitalId(),uuid(a,"hospitalId")))deny();
        for(String field:List.of("asset","depreciation","expense")) {
            UUID accountId=uuid(a,field+"AccountId");
            if(accountId!=null) a.put(field+"AccountLabel",db.queryForObject("SELECT account_number || ' · ' || label FROM accounting_accounts WHERE id=:id",p("id",accountId),String.class));
        }
        return a;
    }
    private List<Map<String,Object>> rows(String sql,MapSqlParameterSource params) {
        return db.query(sql,params,(rs,n)->{
            var row=new LinkedHashMap<String,Object>(); var m=rs.getMetaData();
            for(int i=1;i<=m.getColumnCount();i++) {
                var parts=m.getColumnLabel(i).split("_");var name=new StringBuilder(parts[0]);for(int j=1;j<parts.length;j++)name.append(Character.toUpperCase(parts[j].charAt(0))).append(parts[j].substring(1));
                Object v=rs.getObject(i);if(v instanceof java.sql.Date date)v=date.toLocalDate().toString();else if(v instanceof java.sql.Timestamp t)v=t.toInstant().toString();else if(v instanceof UUID uuid)v=uuid.toString();row.put(name.toString(),v);
            }return row;
        });
    }
    private static MapSqlParameterSource p(Object... values) { var p=new MapSqlParameterSource();for(int i=0;i<values.length;i+=2)p.addValue(values[i].toString(),values[i+1]);return p; }
    private static UUID uuid(Map<String,Object> a,String k) { return a.get(k)==null?null:UUID.fromString(a.get(k).toString()); }
    private static void conflict(String reason) { throw new ResponseStatusException(HttpStatus.CONFLICT,reason); }
    private static void deny() { throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Ce bien appartient à un autre hôpital."); }
}
