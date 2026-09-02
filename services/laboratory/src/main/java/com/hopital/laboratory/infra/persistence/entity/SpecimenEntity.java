package com.hopital.laboratory.infra.persistence.entity;

import com.hopital.laboratory.application.domain.SpecimenStatus;
import com.hopital.laboratory.application.domain.SpecimenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "specimens")
public class SpecimenEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_request_id", nullable = false)
    private AnalysisRequestEntity analysisRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "specimen_type", nullable = false, length = 30)
    private SpecimenType specimenType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SpecimenStatus status;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "collected_by", length = 100)
    private String collectedBy;

    @Column(name = "collection_note", length = 1000)
    private String collectionNote;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    @Column(name = "dispatched_by", length = 100)
    private String dispatchedBy;

    @Column(name = "carrier_name", length = 200)
    private String carrierName;

    @Column(name = "dispatch_note", length = 1000)
    private String dispatchNote;

    @Column(name = "received_by", length = 100)
    private String receivedBy;

    @Column(name = "reception_condition", length = 1000)
    private String receptionCondition;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    protected SpecimenEntity() {
    }

    public SpecimenEntity(
            UUID id,
            String code,
            AnalysisRequestEntity analysisRequest,
            SpecimenType specimenType,
            Instant collectedAt,
            Instant receivedAt) {
        this.id = id;
        this.code = code;
        this.analysisRequest = analysisRequest;
        this.specimenType = specimenType;
        this.status = SpecimenStatus.RECEIVED;
        this.collectedAt = collectedAt;
        this.receivedAt = receivedAt;
    }

    public static SpecimenEntity collectedForReference(
            UUID id,
            String code,
            AnalysisRequestEntity analysisRequest,
            SpecimenType specimenType,
            Instant collectedAt,
            String collectedBy,
            String collectionNote) {
        SpecimenEntity specimen = new SpecimenEntity();
        specimen.id = id;
        specimen.code = code;
        specimen.analysisRequest = analysisRequest;
        specimen.specimenType = specimenType;
        specimen.status = SpecimenStatus.COLLECTED;
        specimen.collectedAt = collectedAt;
        specimen.collectedBy = collectedBy;
        specimen.collectionNote = collectionNote;
        return specimen;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public AnalysisRequestEntity getAnalysisRequest() {
        return analysisRequest;
    }

    public SpecimenType getSpecimenType() {
        return specimenType;
    }

    public SpecimenStatus getStatus() {
        return status;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public String getCollectedBy() {
        return collectedBy;
    }

    public String getCollectionNote() {
        return collectionNote;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }

    public String getDispatchedBy() {
        return dispatchedBy;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public String getDispatchNote() {
        return dispatchNote;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public String getReceptionCondition() {
        return receptionCondition;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void dispatch(Instant dispatchedAt, String dispatchedBy, String carrierName, String dispatchNote) {
        this.status = SpecimenStatus.IN_TRANSIT;
        this.dispatchedAt = dispatchedAt;
        this.dispatchedBy = dispatchedBy;
        this.carrierName = carrierName;
        this.dispatchNote = dispatchNote;
    }

    public void receive(Instant receivedAt, String receivedBy, String receptionCondition) {
        this.status = SpecimenStatus.RECEIVED;
        this.receivedAt = receivedAt;
        this.receivedBy = receivedBy;
        this.receptionCondition = receptionCondition;
        this.rejectionReason = null;
    }

    public void reject(Instant receivedAt, String receivedBy, String rejectionReason) {
        this.status = SpecimenStatus.REJECTED;
        this.receivedAt = receivedAt;
        this.receivedBy = receivedBy;
        this.rejectionReason = rejectionReason;
    }
}
