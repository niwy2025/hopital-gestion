package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AccountingOutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Durable bridge between a completed pharmacy dispense and its accounting
 * document. It deliberately belongs to the patient service because that is
 * where a dispense becomes legally immutable.
 */
@Entity
@Table(name = "patient_pharmacy_accounting_outbox_events")
public class PharmacyDispenseAccountingOutboxEventEntity {

    @Id
    private UUID id;

    @Column(name = "dispense_id", nullable = false, unique = true)
    private UUID dispenseId;

    @Column(name = "dispense_code", nullable = false, unique = true, length = 30)
    private String dispenseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountingOutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "invoice_reference", length = 80)
    private String invoiceReference;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PharmacyDispenseAccountingOutboxEventEntity() {
    }

    public PharmacyDispenseAccountingOutboxEventEntity(
            UUID id,
            UUID dispenseId,
            String dispenseCode,
            Instant createdAt) {
        this.id = id;
        this.dispenseId = dispenseId;
        this.dispenseCode = dispenseCode;
        this.status = AccountingOutboxStatus.PENDING;
        this.attemptCount = 0;
        this.nextAttemptAt = createdAt;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDispenseId() {
        return dispenseId;
    }

    public String getDispenseCode() {
        return dispenseCode;
    }

    public AccountingOutboxStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getInvoiceReference() {
        return invoiceReference;
    }

    public String getLastError() {
        return lastError;
    }

    public void markPosted(String accountingInvoiceReference, Instant processedAt) {
        this.status = AccountingOutboxStatus.POSTED;
        this.processedAt = processedAt;
        this.invoiceReference = accountingInvoiceReference;
        this.lastError = null;
        this.updatedAt = processedAt;
    }

    public void scheduleRetry(Instant nextAttemptAt, String lastError, Instant updatedAt) {
        this.status = AccountingOutboxStatus.PENDING;
        this.attemptCount++;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = lastError;
        this.updatedAt = updatedAt;
    }
}
