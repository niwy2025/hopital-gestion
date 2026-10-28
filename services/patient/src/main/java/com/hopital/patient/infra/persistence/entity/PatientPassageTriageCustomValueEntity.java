package com.hopital.patient.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "patient_passage_triage_custom_values")
@IdClass(PatientPassageTriageCustomValueEntity.Pk.class)
public class PatientPassageTriageCustomValueEntity {

    @Id
    @Column(name = "triage_assessment_id", nullable = false)
    private UUID triageAssessmentId;

    @Id
    @Column(name = "measurement_id", nullable = false)
    private UUID measurementId;

    @Column(name = "numeric_value")
    private BigDecimal numericValue;

    @Column(name = "text_value", length = 500)
    private String textValue;

    protected PatientPassageTriageCustomValueEntity() {
    }

    public PatientPassageTriageCustomValueEntity(
            UUID triageAssessmentId,
            UUID measurementId,
            BigDecimal numericValue,
            String textValue) {
        this.triageAssessmentId = triageAssessmentId;
        this.measurementId = measurementId;
        this.numericValue = numericValue;
        this.textValue = textValue;
    }

    public UUID getTriageAssessmentId() { return triageAssessmentId; }
    public UUID getMeasurementId() { return measurementId; }
    public BigDecimal getNumericValue() { return numericValue; }
    public String getTextValue() { return textValue; }

    public static class Pk implements Serializable {
        private UUID triageAssessmentId;
        private UUID measurementId;

        public Pk() {
        }

        public Pk(UUID triageAssessmentId, UUID measurementId) {
            this.triageAssessmentId = triageAssessmentId;
            this.measurementId = measurementId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Pk pk)) return false;
            return Objects.equals(triageAssessmentId, pk.triageAssessmentId)
                    && Objects.equals(measurementId, pk.measurementId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(triageAssessmentId, measurementId);
        }
    }
}
