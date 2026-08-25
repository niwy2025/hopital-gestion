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

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

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
}
