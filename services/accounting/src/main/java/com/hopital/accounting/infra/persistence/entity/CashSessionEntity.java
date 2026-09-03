package com.hopital.accounting.infra.persistence.entity;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.CashSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** A cashier's opening and closing control. Cash payments are reconciled from posted receipts. */
@Entity
@Table(name = "cash_sessions")
public class CashSessionEntity {
    @Id private UUID id;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @Column(nullable = false, length = 50) private String code;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 3) private AccountingCurrency currency;
    @Column(name = "opening_amount", nullable = false, precision = 18, scale = 2) private BigDecimal openingAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private CashSessionStatus status;
    @Column(name = "opened_at", nullable = false) private Instant openedAt;
    @Column(name = "opened_by_user_id", nullable = false, length = 100) private String openedByUserId;
    @Column(name = "opened_by_username", nullable = false, length = 150) private String openedByUsername;
    @Column(name = "closed_at") private Instant closedAt;
    @Column(name = "closed_by_user_id", length = 100) private String closedByUserId;
    @Column(name = "closed_by_username", length = 150) private String closedByUsername;
    @Column(name = "expected_closing_amount", precision = 18, scale = 2) private BigDecimal expectedClosingAmount;
    @Column(name = "declared_closing_amount", precision = 18, scale = 2) private BigDecimal declaredClosingAmount;
    @Column(name = "variance_amount", precision = 18, scale = 2) private BigDecimal varianceAmount;
    @Column(name = "closing_notes", length = 2000) private String closingNotes;

    protected CashSessionEntity() { }
    public CashSessionEntity(UUID id, UUID hospitalId, String hospitalCode, String code, AccountingCurrency currency,
            BigDecimal openingAmount, String userId, String username, Instant openedAt) {
        this.id = id; this.hospitalId = hospitalId; this.hospitalCode = hospitalCode; this.code = code; this.currency = currency;
        this.openingAmount = openingAmount; this.status = CashSessionStatus.OPEN; this.openedByUserId = userId;
        this.openedByUsername = username; this.openedAt = openedAt;
    }
    public UUID getId() { return id; } public UUID getHospitalId() { return hospitalId; } public String getHospitalCode() { return hospitalCode; }
    public String getCode() { return code; } public AccountingCurrency getCurrency() { return currency; } public BigDecimal getOpeningAmount() { return openingAmount; }
    public CashSessionStatus getStatus() { return status; } public Instant getOpenedAt() { return openedAt; } public String getOpenedByUsername() { return openedByUsername; }
    public Instant getClosedAt() { return closedAt; } public String getClosedByUsername() { return closedByUsername; }
    public BigDecimal getExpectedClosingAmount() { return expectedClosingAmount; } public BigDecimal getDeclaredClosingAmount() { return declaredClosingAmount; }
    public BigDecimal getVarianceAmount() { return varianceAmount; } public String getClosingNotes() { return closingNotes; }
    public void close(BigDecimal expectedClosingAmount, BigDecimal declaredClosingAmount, String notes, String userId, String username, Instant at) {
        this.status = CashSessionStatus.CLOSED; this.expectedClosingAmount = expectedClosingAmount; this.declaredClosingAmount = declaredClosingAmount;
        this.varianceAmount = declaredClosingAmount.subtract(expectedClosingAmount); this.closingNotes = notes;
        this.closedByUserId = userId; this.closedByUsername = username; this.closedAt = at;
    }
}
