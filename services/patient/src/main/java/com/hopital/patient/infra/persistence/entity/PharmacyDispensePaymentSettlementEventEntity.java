package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.PaymentCurrency;
import com.hopital.patient.application.domain.PharmacyDispenseAccountingEventType;
import com.hopital.patient.application.domain.PharmacyDispenseAccountingInvoiceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Immutable receipt of an accounting invoice-state event. The event id is the
 * primary idempotency key; payment id additionally guards against a broken
 * sender issuing different event ids for the same payment.
 */
@Entity
@Table(name = "patient_pharmacy_dispense_payment_settlement_events")
public class PharmacyDispensePaymentSettlementEventEntity {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispense_id", nullable = false)
    private PatientPassagePrescriptionDispenseEntity dispense;

    @Column(name = "payment_id", unique = true)
    private UUID paymentId;

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "invoice_code", nullable = false, length = 80)
    private String invoiceCode;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "due_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal dueAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private PaymentCurrency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false, length = 30)
    private PharmacyDispenseAccountingInvoiceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private PharmacyDispenseAccountingEventType eventType;

    @Column(name = "state_version", nullable = false)
    private long stateVersion;

    @Column(name = "paid_on")
    private LocalDate paidOn;

    @Column(name = "payment_reference", length = 160)
    private String paymentReference;

    @Column(nullable = false)
    private boolean applied;

    @Column(name = "ignored_reason", length = 250)
    private String ignoredReason;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected PharmacyDispensePaymentSettlementEventEntity() {
    }

    public PharmacyDispensePaymentSettlementEventEntity(
            UUID eventId,
            PatientPassagePrescriptionDispenseEntity dispense,
            UUID paymentId,
            UUID invoiceId,
            String invoiceCode,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal dueAmount,
            PaymentCurrency currency,
            PharmacyDispenseAccountingInvoiceStatus status,
            PharmacyDispenseAccountingEventType eventType,
            long stateVersion,
            LocalDate paidOn,
            String paymentReference,
            boolean applied,
            String ignoredReason,
            Instant receivedAt) {
        this.eventId = eventId;
        this.dispense = dispense;
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.invoiceCode = invoiceCode;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.dueAmount = dueAmount;
        this.currency = currency;
        this.status = status;
        this.eventType = eventType;
        this.stateVersion = stateVersion;
        this.paidOn = paidOn;
        this.paymentReference = paymentReference;
        this.applied = applied;
        this.ignoredReason = ignoredReason;
        this.receivedAt = receivedAt;
    }

    public UUID getEventId() { return eventId; }
    public PatientPassagePrescriptionDispenseEntity getDispense() { return dispense; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getInvoiceId() { return invoiceId; }
    public String getInvoiceCode() { return invoiceCode; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public BigDecimal getDueAmount() { return dueAmount; }
    public PaymentCurrency getCurrency() { return currency; }
    public PharmacyDispenseAccountingInvoiceStatus getStatus() { return status; }
    public PharmacyDispenseAccountingEventType getEventType() { return eventType; }
    public long getStateVersion() { return stateVersion; }
    public LocalDate getPaidOn() { return paidOn; }
    public String getPaymentReference() { return paymentReference; }
    public boolean isApplied() { return applied; }
    public String getIgnoredReason() { return ignoredReason; }
    public Instant getReceivedAt() { return receivedAt; }
}
