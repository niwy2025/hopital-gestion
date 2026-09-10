package com.hopital.accounting.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.hopital.accounting.application.domain.AuditActor;
import com.hopital.accounting.application.domain.DataAccessScope;
import com.hopital.accounting.application.dto.AccountingEntryResponse;
import com.hopital.accounting.application.dto.CreateAccountingEntryRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@JdbcTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@Import({FixedAssetService.class,FixedAssetPersistenceTest.ClientConfiguration.class})
@EnabledIfEnvironmentVariable(named="PATRIMONY_POSTGRES_TEST",matches="true")
class FixedAssetPersistenceTest {
    @Autowired FixedAssetService service; @Autowired JdbcTemplate db;
    @MockBean AccountingApplicationService accounting;
    static String sourceJson;
    UUID key=UUID.randomUUID(),hospital=UUID.randomUUID(),account,amortization,expense;
    DataAccessScope scope=new DataAccessScope(false,false,hospital,"HOP-TEST");
    AuditActor actor=new AuditActor("comptable","comptable.test");
    @TestConfiguration static class ClientConfiguration {
        @Bean RestClient.Builder client() {
            return RestClient.builder().requestInterceptor((request,body,execution)->{
                var response=new MockClientHttpResponse(sourceJson.getBytes(java.nio.charset.StandardCharsets.UTF_8),HttpStatus.OK);
                response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);return response;
            });
        }
    }
    @BeforeEach void prepare() {
        source(0,"USD","AVAILABLE","100.00");service.importAsset(key);
        account=account("2441");amortization=account("2844");expense=account("6813");
    }
    void source(int version,String currency,String status,String cost) {
        sourceJson="""
                {"id":"%s","hospitalId":"%s","code":"INV-TEST","name":"Lit test","purchaseCost":%s,
                "currency":"%s","receivedOn":"2025-01-01","status":"%s","version":%d}
                """.formatted(key,hospital,cost,currency,status,version);
    }
    UUID account(String number) {
        UUID id=UUID.randomUUID();
        db.update("INSERT INTO accounting_accounts(id,hospital_id,hospital_code,account_number,label,account_class,nature,created_at) VALUES(?,?,'HOP-TEST',?,'Compte test','2','ASSET',now())",id,hospital,number);
        return id;
    }
    FixedAssetService.Classification classification(LocalDate date,int months) {
        return new FixedAssetService.Classification("IMMOBILIZATION",new BigDecimal("100"),BigDecimal.ZERO,date,months,account,amortization,expense,true,"Justification",0);
    }
    @Test void importsIdempotentlyAndKeepsReviewedValuesAndCurrency() {
        service.classify(key,classification(LocalDate.of(2025,1,1),3),scope,actor);
        source(2,"CDF","MAINTENANCE","200");service.importAsset(key);service.importAsset(key);
        var a=service.get(key,scope);
        assertThat(a.get("sourceCost")).isEqualTo(new BigDecimal("200.00"));assertThat(a.get("accountingValue")).isEqualTo(new BigDecimal("100.00"));
        assertThat(a.get("currency")).isEqualTo("USD");assertThat(a.get("sourceCurrency")).isEqualTo("CDF");
        source(1,"USD","AVAILABLE","50");service.importAsset(key);
        assertThat(service.get(key,scope).get("sourceVersion")).isEqualTo(2);
        assertThat(db.queryForObject("SELECT count(*) FROM accounting_fixed_assets",Integer.class)).isEqualTo(1);
    }
    @Test void restrictsHospitalAndPaginatesRegisterAndSchedule() {
        assertThat(service.search(0,1,"lit","PENDING",null,scope).totalElements()).isEqualTo(1);
        assertThat(service.search(0,1,"lit","PENDING",null,new DataAccessScope(false,false,UUID.randomUUID(),"OTHER")).totalElements()).isZero();
        assertThatThrownBy(()->service.get(key,new DataAccessScope(true,false,UUID.randomUUID(),"OTHER"))).isInstanceOf(ResponseStatusException.class);
        service.classify(key,classification(LocalDate.of(2025,1,17),3),scope,actor);
        var plan=service.schedule(key,0,2,scope);
        assertThat(plan.content()).hasSize(2);assertThat(plan.totalElements()).isEqualTo(3);
        assertThat(plan.content().get(0).get("date")).isEqualTo("2025-01-31");
        BigDecimal total=service.schedule(key,0,20,scope).content().stream().map(r->(BigDecimal)r.get("amount")).reduce(BigDecimal.ZERO,BigDecimal::add);
        assertThat(total).isEqualByComparingTo("100");
        assertThat(service.schedule(key,1,2,scope).content()).hasSize(1);
    }
    @Test void validatesAccountsAndOptimisticVersion() {
        var wrong=new FixedAssetService.Classification("IMMOBILIZATION",BigDecimal.TEN,BigDecimal.ZERO,LocalDate.now(),12,amortization,amortization,expense,true,"Test",0);
        assertThatThrownBy(()->service.classify(key,wrong,scope,actor)).hasMessageContaining("hors amortissements");
        db.update("UPDATE accounting_accounts SET hospital_id=? WHERE id=?",UUID.randomUUID(),amortization);
        assertThatThrownBy(()->service.classify(key,classification(LocalDate.now(),12),scope,actor)).hasMessageContaining("compte actif");
        var expenseTreatment=new FixedAssetService.Classification("EXPENSE",BigDecimal.TEN,BigDecimal.ZERO,null,null,UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),false,"Petit matériel",0);
        var row=service.classify(key,expenseTreatment,scope,actor);
        assertThat(row.get("assetAccountId")).isNull();assertThat(row.get("depreciationAccountId")).isNull();
        assertThatThrownBy(()->service.classify(key,expenseTreatment,scope,actor)).hasMessageContaining("modifiée");
    }
    @Test void refusesFutureRetiredOrCurrencyChangedInstallments() {
        service.classify(key,classification(LocalDate.now().plusMonths(2),3),scope,actor);
        assertThatThrownBy(()->service.prepareInstallment(key,new FixedAssetService.Installment(UUID.randomUUID(),1),scope,actor)).hasMessageContaining("pas encore");
        source(1,"USD","RETIRED","100");
        assertThatThrownBy(()->service.prepareInstallment(key,new FixedAssetService.Installment(UUID.randomUUID(),1),scope,actor)).hasMessageContaining("sorti du patrimoine");
        source(2,"CDF","AVAILABLE","100");
        assertThatThrownBy(()->service.prepareInstallment(key,new FixedAssetService.Installment(UUID.randomUUID(),1),scope,actor)).hasMessageContaining("devise");
        verifyNoInteractions(accounting);
    }
    @Test void preparesExactlyOneBalancedDraftForInstallment() {
        service.classify(key,classification(LocalDate.of(2025,1,1),3),scope,actor);
        UUID journal=UUID.randomUUID(),period=UUID.randomUUID(),entry=UUID.randomUUID();
        db.update("INSERT INTO accounting_journals(id,hospital_id,hospital_code,code,label,type,created_at) VALUES(?,?,'HOP-TEST','OD','Opérations diverses','GENERAL',now())",journal,hospital);
        db.update("INSERT INTO accounting_periods(id,hospital_id,hospital_code,code,label,starts_on,ends_on,status,created_at) VALUES(?,?,'HOP-TEST','2025','Exercice','2025-01-01','2025-12-31','OPEN',now())",period,hospital);
        when(accounting.createManualEntry(any(),any(),any())).thenAnswer(invocation->{
            CreateAccountingEntryRequest r=invocation.getArgument(0);
            assertThat(r.lines()).hasSize(2);assertThat(r.lines().get(0).debit()).isEqualByComparingTo("33.33");
            assertThat(r.lines().get(1).credit()).isEqualByComparingTo("33.33");
            db.update("INSERT INTO accounting_entries(id,hospital_id,hospital_code,period_id,journal_id,journal_code,code,source_type,source_code,entry_date,description,status,currency,total_debit,total_credit,created_at,created_by_user_id,created_by_username) VALUES(?,?,'HOP-TEST',?,?,'OD','ECR-TEST','MANUAL_ENTRY','ECR-TEST','2025-01-31','Amortissement','DRAFT','USD',33.33,33.33,now(),'comptable','comptable')",entry,hospital,period,journal);
            var response=mock(AccountingEntryResponse.class);when(response.id()).thenReturn(entry);return response;
        });
        var first=service.prepareInstallment(key,new FixedAssetService.Installment(journal,1),scope,actor);
        var second=service.prepareInstallment(key,new FixedAssetService.Installment(journal,1),scope,actor);
        assertThat(first.get("entryId").toString()).isEqualTo(second.get("entryId").toString());
        verify(accounting,times(1)).createManualEntry(any(),any(),any());
        assertThat(service.schedule(key,0,20,scope).content().get(0).get("status")).isEqualTo("DRAFT");
        var edit=new FixedAssetService.Classification("IMMOBILIZATION",BigDecimal.TEN,BigDecimal.ZERO,LocalDate.now(),12,account,amortization,expense,true,"Test",1);
        assertThatThrownBy(()->service.classify(key,edit,scope,actor)).hasMessageContaining("déjà des écritures");
    }
}
