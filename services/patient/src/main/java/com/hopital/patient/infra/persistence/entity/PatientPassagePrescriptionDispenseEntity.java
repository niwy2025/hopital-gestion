package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.AccountingSynchronizationStatus;
import com.hopital.patient.application.domain.PaymentCurrency;
import com.hopital.patient.application.domain.PharmacyDispenseAccountingInvoiceStatus;
import com.hopital.patient.application.domain.PrescriptionDispenseCompletion;
import com.hopital.patient.application.domain.PrescriptionPaymentMethod;
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
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "patient_passage_prescription_dispenses")
public class PatientPassagePrescriptionDispenseEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private PatientPassagePrescriptionEntity prescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionDispenseCompletion completion;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "paid_amount", precision = 14, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "total_amount", precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_currency", length = 3)
    private PaymentCurrency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PrescriptionPaymentMethod paymentMethod;

    /**
     * Initial pharmacy-side payment values above are immutable evidence of the
     * dispense. The fields below are a replaceable, versioned projection of
     * the accounting invoice and must be used to show the current balance.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "accounting_sync_status", nullable = false, length = 20)
    private AccountingSynchronizationStatus accountingSynchronizationStatus;

    @Column(name = "accounting_invoice_id")
    private UUID accountingInvoiceId;

    @Column(name = "accounting_invoice_code", length = 80)
    private String accountingInvoiceCode;

    @Column(name = "accounting_total_amount", precision = 14, scale = 2)
    private BigDecimal accountingTotalAmount;

    @Column(name = "accounting_paid_amount", precision = 14, scale = 2)
    private BigDecimal accountingPaidAmount;

    @Column(name = "accounting_due_amount", precision = 14, scale = 2)
    private BigDecimal accountingDueAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounting_currency", length = 3)
    private PaymentCurrency accountingCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounting_invoice_status", length = 30)
    private PharmacyDispenseAccountingInvoiceStatus accountingInvoiceStatus;

    @Column(name = "accounting_state_version")
    private Long accountingStateVersion;

    @Column(name = "accounting_last_payment_id")
    private UUID accountingLastPaymentId;

    @Column(name = "accounting_last_paid_on")
    private LocalDate accountingLastPaidOn;

    @Column(name = "accounting_last_payment_reference", length = 160)
    private String accountingLastPaymentReference;

    @Column(name = "accounting_synchronized_at")
    private Instant accountingSynchronizedAt;

    @Column(name = "dispensed_at", nullable = false)
    private Instant dispensedAt;

    @Column(name = "dispensed_by_user_id", nullable = false, length = 100)
    private String dispensedByUserId;

    @Column(name = "dispensed_by_username", nullable = false, length = 150)
    private String dispensedByUsername;

    protected PatientPassagePrescriptionDispenseEntity() {
    }

    public PatientPassagePrescriptionDispenseEntity(
            UUID id,
            String code,
            PatientPassagePrescriptionEntity prescription,
            PrescriptionDispenseCompletion completion,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            PaymentCurrency currency,
            PrescriptionPaymentMethod paymentMethod,
            String notes,
            AuditActor actor,
            Instant dispensedAt) {
        this.id = id;
        this.code = code;
        this.prescription = prescription;
        this.completion = completion;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.accountingSynchronizationStatus = AccountingSynchronizationStatus.NOT_REQUESTED;
        this.notes = notes;
        this.dispensedAt = dispensedAt;
        this.dispensedByUserId = actor.userId();
        this.dispensedByUsername = actor.username();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public PatientPassagePrescriptionEntity getPrescription() { return prescription; }
    public PrescriptionDispenseCompletion getCompletion() { return completion; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public PaymentCurrency getCurrency() { return currency; }
    public PrescriptionPaymentMethod getPaymentMethod() { return paymentMethod; }
    public AccountingSynchronizationStatus getAccountingSynchronizationStatus() { return accountingSynchronizationStatus; }
    public UUID getAccountingInvoiceId() { return accountingInvoiceId; }
    public String getAccountingInvoiceCode() { return accountingInvoiceCode; }
    public BigDecimal getAccountingTotalAmount() { return accountingTotalAmount; }
    public BigDecimal getAccountingPaidAmount() { return accountingPaidAmount; }
    public BigDecimal getAccountingDueAmount() { return accountingDueAmount; }
    public PaymentCurrency getAccountingCurrency() { return accountingCurrency; }
    public PharmacyDispenseAccountingInvoiceStatus getAccountingInvoiceStatus() { return accountingInvoiceStatus; }
    public Long getAccountingStateVersion() { return accountingStateVersion; }
    public UUID getAccountingLastPaymentId() { return accountingLastPaymentId; }
    public LocalDate getAccountingLastPaidOn() { return accountingLastPaidOn; }
    public String getAccountingLastPaymentReference() { return accountingLastPaymentReference; }
    public Instant getAccountingSynchronizedAt() { return accountingSynchronizedAt; }
    public String getNotes() { return notes; }
    public Instant getDispensedAt() { return dispensedAt; }
    public String getDispensedByUserId() { return dispensedByUserId; }
    public String getDispensedByUsername() { return dispensedByUsername; }

    public void markAccountingPending() {
        if (accountingSynchronizationStatus == AccountingSynchronizationStatus.NOT_REQUESTED) {
            accountingSynchronizationStatus = AccountingSynchronizationStatus.PENDING;
        }
    }

    public boolean hasAccountingProjection() {
        return accountingSynchronizationStatus == AccountingSynchronizationStatus.SYNCHRONIZED
                && accountingInvoiceId != null
                && accountingTotalAmount != null
                && accountingPaidAmount != null
                && accountingDueAmount != null
                && accountingCurrency != null
                && accountingInvoiceStatus != null
                && accountingStateVersion != null;
    }

    public void applyAccountingProjection(
            UUID invoiceId,
            String invoiceCode,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal dueAmount,
            PaymentCurrency projectionCurrency,
            PharmacyDispenseAccountingInvoiceStatus invoiceStatus,
            long stateVersion,
            UUID paymentId,
            LocalDate paidOn,
            String paymentReference,
            Instant synchronizedAt) {
        this.accountingInvoiceId = invoiceId;
        this.accountingInvoiceCode = invoiceCode;
        this.accountingTotalAmount = totalAmount;
        this.accountingPaidAmount = paidAmount;
        this.accountingDueAmount = dueAmount;
        this.accountingCurrency = projectionCurrency;
        this.accountingInvoiceStatus = invoiceStatus;
        this.accountingStateVersion = stateVersion;
        this.accountingLastPaymentId = paymentId;
        this.accountingLastPaidOn = paidOn;
        this.accountingLastPaymentReference = paymentReference;
        this.accountingSynchronizationStatus = AccountingSynchronizationStatus.SYNCHRONIZED;
        this.accountingSynchronizedAt = synchronizedAt;
    }

    public BigDecimal getEffectiveTotalAmount() {
        return hasAccountingProjection() ? accountingTotalAmount : totalAmount;
    }

    public BigDecimal getEffectivePaidAmount() {
        return hasAccountingProjection() ? accountingPaidAmount : paidAmount;
    }

    public BigDecimal getEffectiveDueAmount() {
        if (hasAccountingProjection()) {
            return accountingDueAmount;
        }
        if (totalAmount == null || paidAmount == null) {
            return null;
        }
        return totalAmount.subtract(paidAmount);
    }
}
