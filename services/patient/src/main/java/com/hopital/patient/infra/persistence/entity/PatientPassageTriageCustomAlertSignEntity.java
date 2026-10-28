package com.hopital.patient.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "patient_passage_triage_custom_alert_signs")
@IdClass(PatientPassageTriageCustomAlertSignEntity.Pk.class)
public class PatientPassageTriageCustomAlertSignEntity {

    @Id
    @Column(name = "triage_assessment_id", nullable = false)
    private UUID triageAssessmentId;

    @Id
    @Column(name = "alert_sign_id", nullable = false)
    private UUID alertSignId;

    protected PatientPassageTriageCustomAlertSignEntity() {
    }

    public PatientPassageTriageCustomAlertSignEntity(UUID triageAssessmentId, UUID alertSignId) {
        this.triageAssessmentId = triageAssessmentId;
        this.alertSignId = alertSignId;
    }

    public UUID getTriageAssessmentId() { return triageAssessmentId; }
    public UUID getAlertSignId() { return alertSignId; }

    public static class Pk implements Serializable {
        private UUID triageAssessmentId;
        private UUID alertSignId;

        public Pk() {
        }

        public Pk(UUID triageAssessmentId, UUID alertSignId) {
            this.triageAssessmentId = triageAssessmentId;
            this.alertSignId = alertSignId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Pk pk)) return false;
            return Objects.equals(triageAssessmentId, pk.triageAssessmentId)
                    && Objects.equals(alertSignId, pk.alertSignId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(triageAssessmentId, alertSignId);
        }
    }
}
