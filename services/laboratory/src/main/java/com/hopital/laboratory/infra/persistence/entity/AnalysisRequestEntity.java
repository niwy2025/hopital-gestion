package com.hopital.laboratory.infra.persistence.entity;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.LaboratoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "analysis_requests")
public class AnalysisRequestEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "laboratory_type", nullable = false, length = 30)
    private LaboratoryType laboratoryType;

    @Column(name = "laboratory_code", nullable = false, length = 30)
    private String laboratoryCode;

    @Column(name = "patient_reference", nullable = false, length = 100)
    private String patientReference;

    @Nationalized
    @Column(name = "patient_name", nullable = false, length = 200)
    private String patientName;

    @Column(name = "analysis_code", nullable = false, length = 50)
    private String analysisCode;

    @Nationalized
    @Column(name = "analysis_name", nullable = false, length = 200)
    private String analysisName;

    @Nationalized
    @Column(name = "requester_name", length = 200)
    private String requesterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AnalysisRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AnalysisRequestEntity() {
    }

    public AnalysisRequestEntity(
            UUID id,
            String code,
            LaboratoryType laboratoryType,
            String laboratoryCode,
            String patientReference,
            String patientName,
            String analysisCode,
            String analysisName,
            String requesterName,
            Instant createdAt) {
        this.id = id;
        this.code = code;
        this.laboratoryType = laboratoryType;
        this.laboratoryCode = laboratoryCode;
        this.patientReference = patientReference;
        this.patientName = patientName;
        this.analysisCode = analysisCode;
        this.analysisName = analysisName;
        this.requesterName = requesterName;
        this.status = AnalysisRequestStatus.REQUESTED;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public LaboratoryType getLaboratoryType() {
        return laboratoryType;
    }

    public String getLaboratoryCode() {
        return laboratoryCode;
    }

    public String getPatientReference() {
        return patientReference;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getAnalysisCode() {
        return analysisCode;
    }

    public String getAnalysisName() {
        return analysisName;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public AnalysisRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markSampleReceived() {
        this.status = AnalysisRequestStatus.SAMPLE_RECEIVED;
    }

    public void markResultEntered() {
        this.status = AnalysisRequestStatus.RESULT_ENTERED;
    }

    public void markValidated() {
        this.status = AnalysisRequestStatus.VALIDATED;
    }
}
