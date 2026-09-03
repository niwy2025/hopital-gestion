package com.hopital.accounting.infra.persistence.entity;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingEntryStatus;
import com.hopital.accounting.application.domain.AccountingSourceType;
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

/** Header of an accounting voucher. Lines are immutable as soon as it is posted. */
@Entity
@Table(name = "accounting_entries")
public class AccountingEntryEntity {
    @Id private UUID id;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @Column(name = "period_id", nullable = false) private UUID periodId;
    @Column(name = "journal_id", nullable = false) private UUID journalId;
    @Column(name = "journal_code", nullable = false, length = 20) private String journalCode;
    @Column(nullable = false, length = 50) private String code;
    @Enumerated(EnumType.STRING) @Column(name = "source_type", nullable = false, length = 30) private AccountingSourceType sourceType;
    @Column(name = "source_code", nullable = false, length = 80) private String sourceCode;
    @Column(name = "entry_date", nullable = false) private LocalDate entryDate;
    @Column(nullable = false, length = 1000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AccountingEntryStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 3) private AccountingCurrency currency;
    @Column(name = "total_debit", nullable = false, precision = 18, scale = 2) private BigDecimal totalDebit;
    @Column(name = "total_credit", nullable = false, precision = 18, scale = 2) private BigDecimal totalCredit;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by_user_id", nullable = false, length = 100) private String createdByUserId;
    @Column(name = "created_by_username", nullable = false, length = 150) private String createdByUsername;
    @Column(name = "posted_at") private Instant postedAt;
    @Column(name = "posted_by_user_id", length = 100) private String postedByUserId;
    @Column(name = "posted_by_username", length = 150) private String postedByUsername;
    @Column(name = "reversal_entry_id") private UUID reversalEntryId;

    protected AccountingEntryEntity() { }
    public AccountingEntryEntity(UUID id, UUID hospitalId, String hospitalCode, UUID periodId, UUID journalId,
            String journalCode, String code, AccountingSourceType sourceType, String sourceCode, LocalDate entryDate,
            String description, AccountingCurrency currency, BigDecimal totalDebit, BigDecimal totalCredit,
            String createdByUserId, String createdByUsername, Instant createdAt) {
        this.id = id; this.hospitalId = hospitalId; this.hospitalCode = hospitalCode; this.periodId = periodId;
        this.journalId = journalId; this.journalCode = journalCode; this.code = code; this.sourceType = sourceType;
        this.sourceCode = sourceCode; this.entryDate = entryDate; this.description = description; this.currency = currency;
        this.totalDebit = totalDebit; this.totalCredit = totalCredit; this.status = AccountingEntryStatus.DRAFT;
        this.createdByUserId = createdByUserId; this.createdByUsername = createdByUsername; this.createdAt = createdAt;
    }
    public UUID getId() { return id; } public UUID getHospitalId() { return hospitalId; } public String getHospitalCode() { return hospitalCode; }
    public UUID getPeriodId() { return periodId; } public UUID getJournalId() { return journalId; } public String getJournalCode() { return journalCode; }
    public String getCode() { return code; } public AccountingSourceType getSourceType() { return sourceType; } public String getSourceCode() { return sourceCode; }
    public LocalDate getEntryDate() { return entryDate; } public String getDescription() { return description; } public AccountingEntryStatus getStatus() { return status; }
    public AccountingCurrency getCurrency() { return currency; } public BigDecimal getTotalDebit() { return totalDebit; } public BigDecimal getTotalCredit() { return totalCredit; }
    public Instant getCreatedAt() { return createdAt; } public String getCreatedByUsername() { return createdByUsername; }
    public Instant getPostedAt() { return postedAt; } public String getPostedByUsername() { return postedByUsername; } public UUID getReversalEntryId() { return reversalEntryId; }
    public boolean isPosted() { return status == AccountingEntryStatus.POSTED; }
    public void post(String userId, String username, Instant at) { this.status = AccountingEntryStatus.POSTED; this.postedByUserId = userId; this.postedByUsername = username; this.postedAt = at; }
    public void markReversed(UUID reversalEntryId) { this.status = AccountingEntryStatus.REVERSED; this.reversalEntryId = reversalEntryId; }
}
