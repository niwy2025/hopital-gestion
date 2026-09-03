package com.hopital.pharmacy.infra.persistence.entity;

import com.hopital.pharmacy.application.domain.StockEntryAccountingOutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Durable outbox separating stock receipt persistence from accounting I/O. */
@Entity
@Table(name = "pharmacy_stock_entry_accounting_outbox_events")
public class StockEntryAccountingOutboxEventEntity {

    @Id
    private UUID id;

    @Column(name = "stock_entry_id", nullable = false, unique = true)
    private UUID stockEntryId;

    @Column(name = "stock_entry_code", nullable = false, unique = true, length = 30)
    private String stockEntryCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StockEntryAccountingOutboxStatus status;

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

    protected StockEntryAccountingOutboxEventEntity() {
    }

    public StockEntryAccountingOutboxEventEntity(UUID id, UUID stockEntryId, String stockEntryCode, Instant createdAt) {
        this.id = id;
        this.stockEntryId = stockEntryId;
        this.stockEntryCode = stockEntryCode;
        this.status = StockEntryAccountingOutboxStatus.PENDING;
        this.attemptCount = 0;
        this.nextAttemptAt = createdAt;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getStockEntryId() { return stockEntryId; }
    public String getStockEntryCode() { return stockEntryCode; }
    public StockEntryAccountingOutboxStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public String getAccountingEntryReference() { return accountingEntryReference; }

    public void markPosted(String entryReference, Instant processedAt) {
        this.status = StockEntryAccountingOutboxStatus.POSTED;
        this.processedAt = processedAt;
        this.accountingEntryReference = entryReference;
        this.lastError = null;
        this.updatedAt = processedAt;
    }

    public void scheduleRetry(Instant nextAttemptAt, String lastError, Instant updatedAt) {
        this.status = StockEntryAccountingOutboxStatus.PENDING;
        this.attemptCount++;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = lastError;
        this.updatedAt = updatedAt;
    }
}
