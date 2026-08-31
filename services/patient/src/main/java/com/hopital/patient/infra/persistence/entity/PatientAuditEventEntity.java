package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.PatientAuditEventType;
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
@Table(name = "patient_audit_events")
public class PatientAuditEventEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private PatientAuditEventType type;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "operator_user_id", nullable = false, length = 100)
    private String operatorUserId;

    @Column(name = "operator_username", nullable = false, length = 150)
    private String operatorUsername;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected PatientAuditEventEntity() {
    }

    PatientAuditEventEntity(
            UUID id,
            PatientEntity patient,
            PatientAuditEventType type,
            String description,
            String operatorUserId,
            String operatorUsername,
            Instant occurredAt) {
        this.id = id;
        this.patient = patient;
        this.type = type;
        this.description = description;
        this.operatorUserId = operatorUserId;
        this.operatorUsername = operatorUsername;
        this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public PatientAuditEventType getType() { return type; }
    public String getDescription() { return description; }
    public String getOperatorUsername() { return operatorUsername; }
    public Instant getOccurredAt() { return occurredAt; }
}
