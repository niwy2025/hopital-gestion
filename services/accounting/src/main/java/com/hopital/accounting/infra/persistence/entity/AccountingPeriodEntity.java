package com.hopital.accounting.infra.persistence.entity;

import com.hopital.accounting.application.domain.AccountingPeriodStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "accounting_periods")
public class AccountingPeriodEntity {
    @Id private UUID id;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @Column(nullable = false, length = 40) private String code;
    @Column(nullable = false, length = 180) private String label;
    @Column(name = "starts_on", nullable = false) private LocalDate startsOn;
    @Column(name = "ends_on", nullable = false) private LocalDate endsOn;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AccountingPeriodStatus status;
    @Column(name = "closed_at") private Instant closedAt;
    @Column(name = "closed_by_user_id", length = 100) private String closedByUserId;
    @Column(name = "closed_by_username", length = 150) private String closedByUsername;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AccountingPeriodEntity() { }
    public AccountingPeriodEntity(UUID id, UUID hospitalId, String hospitalCode, String code, String label,
            LocalDate startsOn, LocalDate endsOn, Instant createdAt) {
        this.id = id; this.hospitalId = hospitalId; this.hospitalCode = hospitalCode; this.code = code; this.label = label;
        this.startsOn = startsOn; this.endsOn = endsOn; this.status = AccountingPeriodStatus.OPEN; this.createdAt = createdAt;
    }
    public UUID getId() { return id; } public UUID getHospitalId() { return hospitalId; } public String getHospitalCode() { return hospitalCode; }
    public String getCode() { return code; } public String getLabel() { return label; } public LocalDate getStartsOn() { return startsOn; }
    public LocalDate getEndsOn() { return endsOn; } public AccountingPeriodStatus getStatus() { return status; } public Instant getClosedAt() { return closedAt; }
    public String getClosedByUsername() { return closedByUsername; } public Instant getCreatedAt() { return createdAt; }
    public boolean contains(LocalDate date) { return !date.isBefore(startsOn) && !date.isAfter(endsOn); }
    public void close(String userId, String username, Instant at) { this.status = AccountingPeriodStatus.CLOSED; this.closedByUserId = userId; this.closedByUsername = username; this.closedAt = at; }
}
