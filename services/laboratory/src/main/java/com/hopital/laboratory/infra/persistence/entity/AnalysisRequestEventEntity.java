package com.hopital.laboratory.infra.persistence.entity;

import com.hopital.laboratory.application.domain.AnalysisRequestEventType;
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

/** Immutable traceability event. Workflow state remains on the request/specimen. */
@Entity
@Table(name = "analysis_request_events")
public class AnalysisRequestEventEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_request_id", nullable = false)
    private AnalysisRequestEntity analysisRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AnalysisRequestEventType type;

    @Column(name = "actor_username", length = 100)
    private String actorUsername;

    @Column(length = 1000)
    private String note;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AnalysisRequestEventEntity() {
    }

    public AnalysisRequestEventEntity(
            UUID id,
            AnalysisRequestEntity analysisRequest,
            AnalysisRequestEventType type,
            String actorUsername,
            String note,
            Instant occurredAt) {
        this.id = id;
        this.analysisRequest = analysisRequest;
        this.type = type;
        this.actorUsername = actorUsername;
        this.note = note;
        this.occurredAt = occurredAt;
    }

    public AnalysisRequestEventType getType() {
        return type;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getNote() {
        return note;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
