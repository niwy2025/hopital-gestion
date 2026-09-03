package com.hopital.accounting.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounting_entry_lines")
public class AccountingEntryLineEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "entry_id", nullable = false) private AccountingEntryEntity entry;
    @Column(name = "line_number", nullable = false) private int lineNumber;
    @Column(name = "account_id", nullable = false) private UUID accountId;
    @Column(name = "account_number", nullable = false, length = 20) private String accountNumber;
    @Column(name = "account_label", nullable = false, length = 220) private String accountLabel;
    @Column(nullable = false, length = 1000) private String label;
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal debit;
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal credit;
    @Column(name = "third_party_reference", length = 100) private String thirdPartyReference;

    protected AccountingEntryLineEntity() { }
    public AccountingEntryLineEntity(UUID id, AccountingEntryEntity entry, int lineNumber, AccountingAccountEntity account,
            String label, BigDecimal debit, BigDecimal credit, String thirdPartyReference) {
        this.id = id; this.entry = entry; this.lineNumber = lineNumber; this.accountId = account.getId();
        this.accountNumber = account.getAccountNumber(); this.accountLabel = account.getLabel(); this.label = label;
        this.debit = debit; this.credit = credit; this.thirdPartyReference = thirdPartyReference;
    }
    public UUID getId() { return id; } public AccountingEntryEntity getEntry() { return entry; } public int getLineNumber() { return lineNumber; }
    public UUID getAccountId() { return accountId; } public String getAccountNumber() { return accountNumber; } public String getAccountLabel() { return accountLabel; }
    public String getLabel() { return label; } public BigDecimal getDebit() { return debit; } public BigDecimal getCredit() { return credit; }
    public String getThirdPartyReference() { return thirdPartyReference; }
}
