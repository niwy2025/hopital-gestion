package com.hopital.patient.application.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
public class PatientHospitalizationOutboxService {
    private final JdbcTemplate db; private final RestClient organization;
    public PatientHospitalizationOutboxService(JdbcTemplate db,RestClient.Builder builder,
            @Value("${hospital.organization-service.base-url}") String url) {
        this.db=db;
        var factory=new SimpleClientHttpRequestFactory();factory.setConnectTimeout(2000);factory.setReadTimeout(5000);
        organization=builder.clone().baseUrl(url).requestFactory(factory).build();
    }
    @Transactional(propagation=Propagation.MANDATORY)
    public void enqueue(UUID passage) {
        db.update("""
            INSERT INTO patient_hospitalization_outbox(id,passage_id) VALUES(?,?)
            ON CONFLICT(passage_id) DO UPDATE SET status='PENDING',retry_at=now(),delivered_at=NULL
            """,UUID.randomUUID(),passage);
    }
    @Transactional
    public void deliver(UUID id) {
        var rows=db.queryForList("SELECT passage_id FROM patient_hospitalization_outbox WHERE id=? AND status='PENDING' FOR UPDATE SKIP LOCKED",id);
        if(rows.isEmpty())return;
        try {
            // Le destinataire relit l'état actuel : un ancien événement ne libère pas un passage rouvert.
            organization.post().uri("/internal/organizations/patrimony/passages/{id}/reconcile",rows.get(0).get("passage_id"))
                .retrieve().toBodilessEntity();
            db.update("UPDATE patient_hospitalization_outbox SET status='DELIVERED',delivered_at=now(),attempts=attempts+1 WHERE id=?",id);
        } catch(RestClientException e) {
            db.update("UPDATE patient_hospitalization_outbox SET attempts=attempts+1,retry_at=now()+interval '30 seconds' WHERE id=?",id);
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Libération du lit différée : le service d'hospitalisation est indisponible.");
        }
    }
}
