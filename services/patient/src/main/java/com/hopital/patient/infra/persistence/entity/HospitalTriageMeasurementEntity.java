package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.TriageMeasurementValueKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hospital_triage_measurements")
public class HospitalTriageMeasurementEntity {

    @Id
    private UUID id;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(length = 40)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_kind", nullable = false, length = 20)
    private TriageMeasurementValueKind valueKind;

    @Column(name = "min_value")
    private BigDecimal minValue;

    @Column(name = "max_value")
    private BigDecimal maxValue;

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

    protected HospitalTriageMeasurementEntity() {
    }

    public HospitalTriageMeasurementEntity(
            UUID id,
            UUID hospitalId,
            String code,
            String label,
            String unit,
            TriageMeasurementValueKind valueKind,
            BigDecimal minValue,
            BigDecimal maxValue,
            int displayOrder,
            AuditActor actor,
            Instant createdAt) {
        this.id = id;
        this.hospitalId = hospitalId;
        this.code = code;
        this.label = label;
        this.unit = unit;
        this.valueKind = valueKind;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.displayOrder = displayOrder;
        this.active = true;
        this.createdAt = createdAt;
        this.createdByUserId = actor.userId();
        this.createdByUsername = actor.username();
    }

    public void update(
            String label,
            String unit,
            TriageMeasurementValueKind valueKind,
            BigDecimal minValue,
            BigDecimal maxValue,
            int displayOrder,
            AuditActor actor,
            Instant updatedAt) {
        this.label = label;
        this.unit = unit;
        this.valueKind = valueKind;
        this.minValue = minValue;
        this.maxValue = maxValue;
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
    public String getUnit() { return unit; }
    public TriageMeasurementValueKind getValueKind() { return valueKind; }
    public BigDecimal getMinValue() { return minValue; }
    public BigDecimal getMaxValue() { return maxValue; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedByUsername() { return createdByUsername; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedByUsername() { return updatedByUsername; }
}
