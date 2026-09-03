package com.hopital.accounting.infra.persistence.entity;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingPaymentMethod;
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

@Entity
@Table(name = "accounting_payments")
public class AccountingPaymentEntity {
    @Id private UUID id;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @Column(nullable = false, length = 50) private String code;
    @Column(name = "invoice_id", nullable = false) private UUID invoiceId;
    @Column(name = "invoice_code", nullable = false, length = 50) private String invoiceCode;
    @Column(name = "paid_on", nullable = false) private LocalDate paidOn;
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 3) private AccountingCurrency currency;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private AccountingPaymentMethod method;
    @Column(name = "payment_reference", length = 150) private String paymentReference;
    @Column(name = "idempotency_key", length = 100) private String idempotencyKey;
    @Column(name = "accounting_entry_id", nullable = false) private UUID accountingEntryId;
    @Column(name = "accounting_entry_code", nullable = false, length = 50) private String accountingEntryCode;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "received_by_user_id", nullable = false, length = 100) private String receivedByUserId;
    @Column(name = "received_by_username", nullable = false, length = 150) private String receivedByUsername;

    protected AccountingPaymentEntity() { }
    public AccountingPaymentEntity(UUID id, UUID hospitalId, String hospitalCode, String code, AccountingInvoiceEntity invoice,
            LocalDate paidOn, BigDecimal amount, AccountingCurrency currency, AccountingPaymentMethod method,
            String paymentReference, String idempotencyKey, AccountingEntryEntity accountingEntry, String userId, String username, Instant createdAt) {
        this.id = id; this.hospitalId = hospitalId; this.hospitalCode = hospitalCode; this.code = code;
        this.invoiceId = invoice.getId(); this.invoiceCode = invoice.getCode(); this.paidOn = paidOn; this.amount = amount;
        this.currency = currency; this.method = method; this.paymentReference = paymentReference; this.idempotencyKey = idempotencyKey;
        this.accountingEntryId = accountingEntry.getId(); this.accountingEntryCode = accountingEntry.getCode();
        this.receivedByUserId = userId; this.receivedByUsername = username; this.createdAt = createdAt;
    }
    public UUID getId() { return id; } public UUID getHospitalId() { return hospitalId; } public String getHospitalCode() { return hospitalCode; }
    public String getCode() { return code; } public UUID getInvoiceId() { return invoiceId; } public String getInvoiceCode() { return invoiceCode; }
    public LocalDate getPaidOn() { return paidOn; } public BigDecimal getAmount() { return amount; } public AccountingCurrency getCurrency() { return currency; }
    public AccountingPaymentMethod getMethod() { return method; } public String getPaymentReference() { return paymentReference; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getAccountingEntryId() { return accountingEntryId; } public String getAccountingEntryCode() { return accountingEntryCode; }
    public Instant getCreatedAt() { return createdAt; } public String getReceivedByUsername() { return receivedByUsername; }
}
