package com.hopital.laboratory.infra.persistence.entity;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.AnalysisPriority;
import com.hopital.laboratory.application.domain.LaboratoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

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

    @Column(name = "patient_name", nullable = false, length = 200)
    private String patientName;

    @Column(name = "patient_passage_id")
    private UUID patientPassageId;

    /** Hospital that created a referral to a provincial reference laboratory. */
    @Column(name = "origin_hospital_id")
    private UUID originHospitalId;

    @Column(name = "origin_hospital_code", length = 30)
    private String originHospitalCode;

    @Column(name = "analysis_code", nullable = false, length = 50)
    private String analysisCode;

    @Column(name = "analysis_name", nullable = false, length = 200)
    private String analysisName;

    @Column(name = "requester_name", length = 200)
    private String requesterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalysisPriority priority;

    @Column(name = "clinical_indication", length = 1000)
    private String clinicalIndication;

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
        this(
                id,
                code,
                laboratoryType,
                laboratoryCode,
                patientReference,
                patientName,
                analysisCode,
                analysisName,
                requesterName,
                createdAt,
                null,
                null,
                null,
                AnalysisPriority.ROUTINE,
                null);
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
            Instant createdAt,
            UUID patientPassageId) {
        this(
                id,
                code,
                laboratoryType,
                laboratoryCode,
                patientReference,
                patientName,
                analysisCode,
                analysisName,
                requesterName,
                createdAt,
                patientPassageId,
                null,
                null,
                AnalysisPriority.ROUTINE,
                null);
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
            Instant createdAt,
            UUID patientPassageId,
            UUID originHospitalId,
            String originHospitalCode,
            AnalysisPriority priority,
            String clinicalIndication) {
        this.id = id;
        this.code = code;
        this.laboratoryType = laboratoryType;
        this.laboratoryCode = laboratoryCode;
        this.patientReference = patientReference;
        this.patientName = patientName;
        this.patientPassageId = patientPassageId;
        this.originHospitalId = originHospitalId;
        this.originHospitalCode = originHospitalCode;
        this.analysisCode = analysisCode;
        this.analysisName = analysisName;
        this.requesterName = requesterName;
        this.priority = priority;
        this.clinicalIndication = clinicalIndication;
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

    public UUID getPatientPassageId() {
        return patientPassageId;
    }

    public UUID getOriginHospitalId() {
        return originHospitalId;
    }

    public String getOriginHospitalCode() {
        return originHospitalCode;
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

    public AnalysisPriority getPriority() {
        return priority;
    }

    public String getClinicalIndication() {
        return clinicalIndication;
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

    public void markSampleCollected() {
        this.status = AnalysisRequestStatus.SAMPLE_COLLECTED;
    }

    public void markSampleInTransit() {
        this.status = AnalysisRequestStatus.SAMPLE_IN_TRANSIT;
    }

    public void markRecollectionRequired() {
        this.status = AnalysisRequestStatus.RECOLLECTION_REQUIRED;
    }

    public void markResultEntered() {
        this.status = AnalysisRequestStatus.RESULT_ENTERED;
    }

    public void markValidated() {
        this.status = AnalysisRequestStatus.VALIDATED;
    }
}
