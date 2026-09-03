package com.hopital.pharmacy.infra.persistence.entity;

import com.hopital.pharmacy.application.domain.StockMovementAccountingOutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Durable hand-off between an already committed stock-out movement and its
 * accounting voucher. The immutable movement remains the source of truth.
 */
@Entity
@Table(name = "pharmacy_stock_movement_accounting_outbox_events")
public class StockMovementAccountingOutboxEventEntity {

    @Id
    private UUID id;

    @Column(name = "stock_movement_id", nullable = false, unique = true)
    private UUID stockMovementId;

    @Column(name = "stock_movement_code", nullable = false, unique = true, length = 30)
    private String stockMovementCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockMovementAccountingOutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "accounting_entry_reference", length = 80)
    private String accountingEntryReference;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StockMovementAccountingOutboxEventEntity() {
    }

    public StockMovementAccountingOutboxEventEntity(
            UUID id,
            UUID stockMovementId,
            String stockMovementCode,
            Instant createdAt) {
        this.id = id;
        this.stockMovementId = stockMovementId;
        this.stockMovementCode = stockMovementCode;
        this.status = StockMovementAccountingOutboxStatus.PENDING;
        this.attemptCount = 0;
        this.nextAttemptAt = createdAt;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getStockMovementId() { return stockMovementId; }
    public String getStockMovementCode() { return stockMovementCode; }
    public StockMovementAccountingOutboxStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public String getAccountingEntryReference() { return accountingEntryReference; }

    public void markPosted(String entryReference, Instant processedAt) {
        this.status = StockMovementAccountingOutboxStatus.POSTED;
        this.processedAt = processedAt;
        this.accountingEntryReference = entryReference;
        this.lastError = null;
        this.updatedAt = processedAt;
    }

    /** Terminal state for a dispense already handled by the patient outbox or a zero-value movement. */
    public void markExcluded(String reason, Instant processedAt) {
        this.status = StockMovementAccountingOutboxStatus.EXCLUDED;
        this.processedAt = processedAt;
        this.accountingEntryReference = null;
        this.lastError = reason;
        this.updatedAt = processedAt;
    }

    public void scheduleRetry(Instant nextAttemptAt, String lastError, Instant updatedAt) {
        this.status = StockMovementAccountingOutboxStatus.PENDING;
        this.attemptCount++;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = lastError;
        this.updatedAt = updatedAt;
    }
}
