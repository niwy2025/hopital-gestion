package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hospital_triage_alert_signs")
public class HospitalTriageAlertSignEntity {

    @Id
    private UUID id;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by_user_id", nullable = false, length = 100)
    private String createdByUserId;

    @Column(name = "created_by_username", nullable = false, length = 150)
    private String createdByUsername;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by_user_id", length = 100)
    private String updatedByUserId;

    @Column(name = "updated_by_username", length = 150)
    private String updatedByUsername;

    protected HospitalTriageAlertSignEntity() {
    }

    public HospitalTriageAlertSignEntity(
            UUID id,
            UUID hospitalId,
            String code,
            String label,
            int displayOrder,
            AuditActor actor,
            Instant createdAt) {
        this.id = id;
        this.hospitalId = hospitalId;
        this.code = code;
        this.label = label;
        this.displayOrder = displayOrder;
        this.active = true;
        this.createdAt = createdAt;
        this.createdByUserId = actor.userId();
        this.createdByUsername = actor.username();
    }

    public void update(String label, int displayOrder, AuditActor actor, Instant updatedAt) {
        this.label = label;
        this.displayOrder = displayOrder;
        this.updatedAt = updatedAt;
        this.updatedByUserId = actor.userId();
        this.updatedByUsername = actor.username();
    }

    public void changeStatus(boolean active, AuditActor actor, Instant updatedAt) {
        this.active = active;
        this.updatedAt = updatedAt;
        this.updatedByUserId = actor.userId();
        this.updatedByUsername = actor.username();
    }

    public UUID getId() { return id; }
    public UUID getHospitalId() { return hospitalId; }
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedByUsername() { return createdByUsername; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedByUsername() { return updatedByUsername; }
}
