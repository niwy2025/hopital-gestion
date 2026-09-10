package com.hopital.patient.application.service;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PatientHospitalizationScheduler {
    private final JdbcTemplate db;private final PatientHospitalizationOutboxService outbox;
    public PatientHospitalizationScheduler(JdbcTemplate db,PatientHospitalizationOutboxService outbox) { this.db=db;this.outbox=outbox; }
    @Scheduled(fixedDelayString="${hospital.hospitalization.release-delay-ms:5000}")
    public void publish() {
        for(UUID id:db.query("SELECT id FROM patient_hospitalization_outbox WHERE status='PENDING' AND retry_at<=now() ORDER BY retry_at LIMIT 10",(r,n)->r.getObject(1,UUID.class))) {
            try { outbox.deliver(id); }
            catch(RuntimeException e) { org.slf4j.LoggerFactory.getLogger(getClass()).warn("Livraison de la sortie différée ; reprise au prochain cycle."); }
        }
    }
}
