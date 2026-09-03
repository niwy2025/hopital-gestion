package com.hopital.accounting.infra.persistence.entity;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.InvoiceStatus;
import com.hopital.accounting.application.domain.PharmacyPaymentSettlementEventType;
import com.hopital.accounting.application.domain.PharmacyPaymentSettlementOutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Durable, at-least-once projection of a pharmacy invoice state to the
 * patient service. The full aggregate state is intentionally snapshotted so
 * the consumer never has to call accounting while applying the event.
 */
@Entity
@Table(name = "accounting_pharmacy_payment_settlement_outbox_events")
public class PharmacyPaymentSettlementOutboxEventEntity {

    @Id
    private UUID id;

    @Column(name = "event_key", nullable = false, unique = true, length = 120)
    private String eventKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private PharmacyPaymentSettlementEventType eventType;

    @Column(name = "payment_id", unique = true)
    private UUID paymentId;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "dispense_code", nullable = false, length = 80)
    private String dispenseCode;

    @Column(name = "invoice_code", nullable = false, length = 50)
    private String invoiceCode;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "due_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal dueAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private AccountingCurrency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false, length = 20)
    private InvoiceStatus invoiceStatus;

    @Column(name = "state_version", nullable = false)
    private int stateVersion;

    @Column(name = "paid_on")
    private LocalDate paidOn;

    @Column(name = "payment_reference", length = 150)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PharmacyPaymentSettlementOutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PharmacyPaymentSettlementOutboxEventEntity() {
    }

    private PharmacyPaymentSettlementOutboxEventEntity(
            UUID id,
            String eventKey,
            PharmacyPaymentSettlementEventType eventType,
            UUID paymentId,
            AccountingInvoiceEntity invoice,
            LocalDate paidOn,
            String paymentReference,
            Instant createdAt) {
        this.id = id;
        this.eventKey = eventKey;
        this.eventType = eventType;
        this.paymentId = paymentId;
        this.invoiceId = invoice.getId();
        this.dispenseCode = invoice.getSourceCode();
        this.invoiceCode = invoice.getCode();
        this.totalAmount = invoice.getTotalAmount();
        this.paidAmount = invoice.getPaidAmount();
        this.dueAmount = invoice.getDueAmount();
        this.currency = invoice.getCurrency();
        this.invoiceStatus = invoice.getStatus();
        this.stateVersion = invoice.getSettlementVersion();
        this.paidOn = paidOn;
        this.paymentReference = paymentReference;
        this.status = PharmacyPaymentSettlementOutboxStatus.PENDING;
        this.attemptCount = 0;
        this.nextAttemptAt = createdAt;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static PharmacyPaymentSettlementOutboxEventEntity invoiceIssued(
            AccountingInvoiceEntity invoice,
            Instant createdAt) {
        return new PharmacyPaymentSettlementOutboxEventEntity(
                UUID.randomUUID(),
                "INVOICE:" + invoice.getId() + ":" + invoice.getSettlementVersion(),
                PharmacyPaymentSettlementEventType.INVOICE_ISSUED,
                null,
                invoice,
                null,
                null,
                createdAt);
    }

    public static PharmacyPaymentSettlementOutboxEventEntity paymentRecorded(
            AccountingPaymentEntity payment,
            AccountingInvoiceEntity invoice,
            Instant createdAt) {
        return new PharmacyPaymentSettlementOutboxEventEntity(
                UUID.randomUUID(),
                "PAYMENT:" + payment.getId(),
                PharmacyPaymentSettlementEventType.PAYMENT_RECORDED,
                payment.getId(),
                invoice,
                payment.getPaidOn(),
                payment.getPaymentReference(),
                createdAt);
    }

    public UUID getId() { return id; }
    public String getEventKey() { return eventKey; }
    public PharmacyPaymentSettlementEventType getEventType() { return eventType; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getInvoiceId() { return invoiceId; }
    public String getDispenseCode() { return dispenseCode; }
    public String getInvoiceCode() { return invoiceCode; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public BigDecimal getDueAmount() { return dueAmount; }
    public AccountingCurrency getCurrency() { return currency; }
    public InvoiceStatus getInvoiceStatus() { return invoiceStatus; }
    public int getStateVersion() { return stateVersion; }
    public LocalDate getPaidOn() { return paidOn; }
    public String getPaymentReference() { return paymentReference; }
    public PharmacyPaymentSettlementOutboxStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public Instant getProcessedAt() { return processedAt; }
    public String getLastError() { return lastError; }

    public void markPosted(Instant processedAt) {
        this.status = PharmacyPaymentSettlementOutboxStatus.POSTED;
        this.processedAt = processedAt;
        this.lastError = null;
        this.updatedAt = processedAt;
    }

    public void scheduleRetry(Instant nextAttemptAt, String lastError, Instant updatedAt) {
        this.status = PharmacyPaymentSettlementOutboxStatus.PENDING;
        this.attemptCount++;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = lastError;
        this.updatedAt = updatedAt;
    }
}
