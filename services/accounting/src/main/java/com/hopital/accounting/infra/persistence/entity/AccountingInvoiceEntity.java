package com.hopital.accounting.infra.persistence.entity;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingSourceType;
import com.hopital.accounting.application.domain.InvoiceStatus;
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
@Table(name = "accounting_invoices")
public class AccountingInvoiceEntity {
    @Id private UUID id;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @Column(nullable = false, length = 50) private String code;
    @Enumerated(EnumType.STRING) @Column(name = "source_type", nullable = false, length = 30) private AccountingSourceType sourceType;
    @Column(name = "source_code", nullable = false, length = 80) private String sourceCode;
    @Column(name = "patient_id") private UUID patientId;
    @Column(name = "patient_code", length = 50) private String patientCode;
    @Column(name = "passage_id") private UUID passageId;
    @Column(name = "passage_code", length = 50) private String passageCode;
    @Column(name = "issued_on", nullable = false) private LocalDate issuedOn;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private InvoiceStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 3) private AccountingCurrency currency;
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2) private BigDecimal totalAmount;
    @Column(name = "paid_amount", nullable = false, precision = 18, scale = 2) private BigDecimal paidAmount;
    @Column(name = "due_amount", nullable = false, precision = 18, scale = 2) private BigDecimal dueAmount;
    @Column(name = "settlement_version", nullable = false) private int settlementVersion;
    @Column(nullable = false, length = 1000) private String description;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by_user_id", nullable = false, length = 100) private String createdByUserId;
    @Column(name = "created_by_username", nullable = false, length = 150) private String createdByUsername;

    protected AccountingInvoiceEntity() { }
    public AccountingInvoiceEntity(UUID id, UUID hospitalId, String hospitalCode, String code, AccountingSourceType sourceType,
            String sourceCode, UUID patientId, String patientCode, UUID passageId, String passageCode, LocalDate issuedOn,
            AccountingCurrency currency, BigDecimal totalAmount, String description, String userId, String username, Instant createdAt) {
        this.id = id; this.hospitalId = hospitalId; this.hospitalCode = hospitalCode; this.code = code; this.sourceType = sourceType;
        this.sourceCode = sourceCode; this.patientId = patientId; this.patientCode = patientCode; this.passageId = passageId;
        this.passageCode = passageCode; this.issuedOn = issuedOn; this.currency = currency; this.totalAmount = totalAmount;
        this.paidAmount = BigDecimal.ZERO.setScale(2); this.dueAmount = totalAmount; this.status = InvoiceStatus.DRAFT;
        this.settlementVersion = 0;
        this.description = description; this.createdByUserId = userId; this.createdByUsername = username; this.createdAt = createdAt;
    }
    public UUID getId() { return id; } public UUID getHospitalId() { return hospitalId; } public String getHospitalCode() { return hospitalCode; }
    public String getCode() { return code; } public AccountingSourceType getSourceType() { return sourceType; } public String getSourceCode() { return sourceCode; }
    public UUID getPatientId() { return patientId; } public String getPatientCode() { return patientCode; } public UUID getPassageId() { return passageId; }
    public String getPassageCode() { return passageCode; } public LocalDate getIssuedOn() { return issuedOn; } public InvoiceStatus getStatus() { return status; }
    public AccountingCurrency getCurrency() { return currency; } public BigDecimal getTotalAmount() { return totalAmount; } public BigDecimal getPaidAmount() { return paidAmount; }
    public BigDecimal getDueAmount() { return dueAmount; } public String getDescription() { return description; } public Instant getCreatedAt() { return createdAt; }
    public String getCreatedByUsername() { return createdByUsername; }
    public int getSettlementVersion() { return settlementVersion; }
    public void issue() {
        if (status != InvoiceStatus.DRAFT) throw new IllegalStateException("Cette facture a déjà été émise.");
        status = totalAmount.signum() == 0 ? InvoiceStatus.PAID : InvoiceStatus.ISSUED;
        settlementVersion++;
    }
    public void receive(BigDecimal amount) {
        if (status == InvoiceStatus.DRAFT) throw new IllegalStateException("La facture doit être émise avant encaissement.");
        if (status == InvoiceStatus.CANCELLED) throw new IllegalStateException("Une facture annulée ne peut pas être encaissée.");
        paidAmount = paidAmount.add(amount); dueAmount = totalAmount.subtract(paidAmount);
        status = dueAmount.signum() == 0 ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID;
        settlementVersion++;
    }
    public void cancel() { if (paidAmount.signum() != 0) throw new IllegalStateException("Une facture encaissée ne peut pas être annulée."); status = InvoiceStatus.CANCELLED; }
}
