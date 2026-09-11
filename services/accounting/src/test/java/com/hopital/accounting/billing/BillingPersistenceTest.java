package com.hopital.accounting.billing;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopital.accounting.application.domain.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@JdbcTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@Import({BillingStore.class,BillingService.class,BillingPersistenceTest.Config.class})
@EnabledIfEnvironmentVariable(named="PATRIMONY_POSTGRES_TEST",matches="true")
class BillingPersistenceTest {
    @Configuration static class Config { @Bean ObjectMapper mapper(){return new ObjectMapper().findAndRegisterModules();} }
    @Autowired BillingService service;
    @Autowired JdbcTemplate db;
    @MockBean BillingReferences references;
    final UUID hospital=UUID.randomUUID(),passage=UUID.randomUUID(),patient=UUID.randomUUID();
    final DataAccessScope scope=new DataAccessScope(false,false,hospital,"HOP-TEST");
    final AuditActor actor=new AuditActor("operator-id","facturation.test");
    final LocalDate today=LocalDate.now(ZoneId.of("Africa/Kinshasa"));
    @BeforeEach void prepare(){reference("OPEN",null);}
    void reference(String status,Instant closed){when(references.passage(passage)).thenReturn(new BillingReferences.Passage(passage,"PAS-TEST",patient,"PAT-TEST","Patient test",hospital,"HOP-TEST",status,today.minusDays(5).atStartOfDay(ZoneId.of("Africa/Kinshasa")).toInstant(),closed));}
    UUID create(){return BillingStore.id(service.create(new BillingRequests.Service(hospital,"Consultation "+UUID.randomUUID(),"CONSULTATION",""),scope,actor),"id");}
    UUID tariff(UUID id,LocalDate date,String currency,String price){return BillingStore.id(service.tariff(id,new BillingRequests.Tariff(date,currency,"ACT",new BigDecimal(price),"Décision tarifaire"),scope,actor),"id");}
    BillingRequests.Charge charge(UUID id,UUID tariff,String quantity,String currency,UUID request){return new BillingRequests.Charge(id,tariff,today,new BigDecimal(quantity),currency,request,"Consultation réalisée");}
    @Test void paginatesSearchAndEnforcesHospitalIsolation(){
        UUID id=create();var row=service.service(id,scope);
        assertThat(row.get("code")).asString().startsWith("PRS-");
        assertThat(service.services(hospital,0,1,"Consultation","CONSULTATION",true,scope).totalElements()).isEqualTo(1);
        assertThat(service.services(hospital,1,1,"Consultation","",null,scope).content()).isEmpty();
        assertThatThrownBy(()->service.services(hospital,0,101,"","",null,scope)).isInstanceOf(ResponseStatusException.class);
        var foreign=new DataAccessScope(true,false,UUID.randomUUID(),"OTHER");
        assertThatThrownBy(()->service.service(id,foreign)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->service.create(new BillingRequests.Service(UUID.randomUUID(),"Other","CARE",""),scope,actor)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->service.create(new BillingRequests.Service(hospital,row.get("name").toString().toUpperCase(),"CARE",""),scope,actor)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->service.workspace(passage,0,20,"","",foreign)).isInstanceOf(ResponseStatusException.class);
    }
    @Test void versionsTariffsAndKeepsPriceAndNameSnapshots(){
        UUID id=create(),rate=tariff(id,today,"CDF","10.25"),request=UUID.randomUUID();
        var line=service.charge(passage,charge(id,rate,"2.5","CDF",request),scope,actor);
        assertThat(line.get("totalAmount")).isEqualTo(new BigDecimal("25.63"));
        String original=line.get("serviceName").toString();
        service.update(id,new BillingRequests.UpdateService("Nom corrigé","CARE","",false,0,"Correction"),scope,actor);
        assertThat(service.charge(passage,charge(id,rate,"2.5","CDF",request),scope,actor)).isEqualTo(line);
        assertThat(line.get("serviceName")).isEqualTo(original);
        assertThatThrownBy(()->service.update(id,new BillingRequests.UpdateService("Nom","CARE","",true,0,"Correction"),scope,actor)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->service.quote(passage,id,today,BigDecimal.ONE,"CDF",scope)).isInstanceOf(ResponseStatusException.class);
        assertThat(service.events(id,0,10,scope).totalElements()).isEqualTo(3);
    }
    @Test void selectsRatesByDateAndCurrencyAndCancelsOnlyFuture(){
        UUID id=create(),current=tariff(id,today,"CDF","20"),future=tariff(id,today.plusDays(1),"CDF","30");tariff(id,today,"USD","1");
        assertThat(service.quote(passage,id,today,BigDecimal.ONE,"CDF",scope).get("totalAmount")).isEqualTo(new BigDecimal("20.00"));
        assertThat(service.quote(passage,id,today,BigDecimal.ONE,"USD",scope).get("totalAmount")).isEqualTo(new BigDecimal("1.00"));
        assertThatThrownBy(()->tariff(id,today.minusDays(1),"CDF","2")).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->tariff(id,today,"CDF","2")).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->service.cancelTariff(id,current,"Erreur",scope,actor)).isInstanceOf(ResponseStatusException.class);
        service.cancelTariff(id,future,"Tarif retiré",scope,actor);tariff(id,today.plusDays(1),"CDF","40");
        assertThatThrownBy(()->service.charge(passage,charge(id,future,"1","CDF",UUID.randomUUID()),scope,actor)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->service.quote(passage,id,today.minusDays(1),BigDecimal.ONE,"CDF",scope)).isInstanceOf(ResponseStatusException.class);
    }
    @Test void cancellationPreservesAuditAndCurrencyTotals(){
        UUID id=create(),cdf=tariff(id,today,"CDF","100"),usd=tariff(id,today,"USD","2");
        var a=service.charge(passage,charge(id,cdf,"1","CDF",UUID.randomUUID()),scope,actor);
        service.charge(passage,charge(id,usd,"1","USD",UUID.randomUUID()),scope,actor);
        assertThat((List<?>)service.workspace(passage,0,20,"","",scope).get("totals")).hasSize(2);
        UUID line=BillingStore.id(a,"id");var cancelled=service.cancelCharge(passage,line,"Saisie erronée",scope,actor);
        assertThat(cancelled.get("status")).isEqualTo("CANCELLED");assertThat(cancelled.get("cancelledById")).isEqualTo(actor.userId());
        assertThat(service.cancelCharge(passage,line,"Autre motif",scope,actor).get("cancellationReason")).isEqualTo("Saisie erronée");
        assertThat((List<?>)service.workspace(passage,0,20,"","",scope).get("totals")).hasSize(1);
        assertThat(db.queryForObject("SELECT count(*) FROM billing_passage_charges WHERE passage_id=?",Integer.class,passage)).isEqualTo(2);
    }
    @Test void validatesPassagePeriodStatusAndRetryPayload(){
        UUID id=create(),rate=tariff(id,today,"CDF","10"),key=UUID.randomUUID();
        service.charge(passage,charge(id,rate,"1","CDF",key),scope,actor);
        assertThatThrownBy(()->service.charge(passage,charge(id,rate,"2","CDF",key),scope,actor)).isInstanceOf(ResponseStatusException.class);
        reference("CLOSED",Instant.now());assertThat(service.quote(passage,id,today,BigDecimal.ONE,"CDF",scope)).isNotEmpty();
        assertThatThrownBy(()->service.quote(passage,id,today.plusDays(1),BigDecimal.ONE,"CDF",scope)).isInstanceOf(ResponseStatusException.class);
        reference("CANCELLED",Instant.now());assertThatThrownBy(()->service.quote(passage,id,today,BigDecimal.ONE,"CDF",scope)).isInstanceOf(ResponseStatusException.class);
    }
    @Test @Transactional(propagation=Propagation.NOT_SUPPORTED)
    void concurrentRetriesCreateExactlyOneLine() throws Exception {
        UUID id=create(),rate=tariff(id,today,"CDF","7"),key=UUID.randomUUID();
        try(var pool=Executors.newFixedThreadPool(2)){
            var gate=new CountDownLatch(1);Callable<Map<String,Object>> task=()->{gate.await();return service.charge(passage,charge(id,rate,"1","CDF",key),scope,actor);};
            var a=pool.submit(task);var b=pool.submit(task);gate.countDown();assertThat(a.get(10,TimeUnit.SECONDS).get("id")).isEqualTo(b.get(10,TimeUnit.SECONDS).get("id"));
            assertThat(db.queryForObject("SELECT count(*) FROM billing_passage_charges WHERE hospital_id=?",Integer.class,hospital)).isEqualTo(1);
        }finally{
            db.update("DELETE FROM billing_passage_charges WHERE hospital_id=?",hospital);db.update("DELETE FROM billing_catalog_events WHERE hospital_id=?",hospital);
            db.update("DELETE FROM billing_tariffs WHERE hospital_id=?",hospital);db.update("DELETE FROM billing_services WHERE hospital_id=?",hospital);
        }
    }
}
