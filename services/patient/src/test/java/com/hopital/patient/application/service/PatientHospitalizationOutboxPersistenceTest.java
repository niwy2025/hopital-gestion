package com.hopital.patient.application.service;

import static org.assertj.core.api.Assertions.*;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

@JdbcTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@Import({PatientHospitalizationOutboxService.class,PatientHospitalizationOutboxPersistenceTest.Config.class})
@EnabledIfEnvironmentVariable(named="TRANSFER_POSTGRES_TEST",matches="true")
class PatientHospitalizationOutboxPersistenceTest {
    static final AtomicInteger calls=new AtomicInteger(),status=new AtomicInteger(204);
    static final HttpServer server=startServer();
    static HttpServer startServer() {
        try {
            var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
            server.createContext("/internal/organizations/patrimony/passages/",exchange->{
                calls.incrementAndGet();exchange.sendResponseHeaders(status.get(),-1);exchange.close();
            });server.start();return server;
        } catch(java.io.IOException e){throw new IllegalStateException(e);}
    }
    @AfterAll static void stop(){server.stop(0);}
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("hospital.organization-service.base-url",()->"http://127.0.0.1:"+server.getAddress().getPort());
    }
    @TestConfiguration static class Config { @Bean RestClient.Builder builder(){return RestClient.builder();} }
    @Autowired JdbcTemplate db;
    @Autowired PatientHospitalizationOutboxService service;
    @Autowired PlatformTransactionManager transactions;
    UUID passage;
    @BeforeEach void fixtures(){
        calls.set(0);status.set(204);UUID patient=UUID.randomUUID();passage=UUID.randomUUID();
        db.update("""
            INSERT INTO patients(id,code,first_name,last_name,date_of_birth,gender,registration_hospital_code,created_at,
                national_identifier,created_by_user_id,created_by_username,updated_by_user_id,updated_by_username,updated_at)
            VALUES(?,?,'Patient','Test','1990-01-01','FEMALE','HOP',now(),?,'test','test','test','test',now())
            """,patient,"PAT-"+patient.toString().substring(0,8),patient.toString());
        db.update("""
            INSERT INTO patient_passages(id,code,patient_id,hospital_id,hospital_code,type,status,arrived_at,created_by_user_id,created_by_username)
            VALUES(?,?,?,?,'HOP','HOSPITALIZATION','CLOSED',now(),'test','test')
            """,passage,"PAS-"+passage.toString().substring(0,8),patient,UUID.randomUUID());
    }
    @Test void retriesThenDeliversOnceAndAllowsLaterClosureOfSamePassage(){
        service.enqueue(passage);service.enqueue(passage);
        UUID id=db.queryForObject("SELECT id FROM patient_hospitalization_outbox WHERE passage_id=?",UUID.class,passage);
        status.set(503);service.deliver(id);
        assertThat(db.queryForObject("SELECT status FROM patient_hospitalization_outbox WHERE id=?",String.class,id)).isEqualTo("PENDING");
        status.set(204);service.deliver(id);service.deliver(id);
        assertThat(calls.get()).isEqualTo(2);
        assertThat(db.queryForObject("SELECT status FROM patient_hospitalization_outbox WHERE id=?",String.class,id)).isEqualTo("DELIVERED");
        service.enqueue(passage);service.deliver(id);assertThat(calls.get()).isEqualTo(3);
    }
    @Test void queueInsertRollsBackWithBusinessTransaction(){
        var tx=new TransactionTemplate(transactions);
        tx.executeWithoutResult(s->{service.enqueue(passage);s.setRollbackOnly();});
        org.springframework.test.context.transaction.TestTransaction.end();
        assertThat(db.queryForObject("SELECT count(*) FROM patient_hospitalization_outbox WHERE passage_id=?",Long.class,passage)).isZero();
        org.springframework.test.context.transaction.TestTransaction.start();
    }
}
