package com.hopital.accounting.infra.persistence.entity;

import com.hopital.accounting.application.domain.AccountNature;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounting_accounts")
public class AccountingAccountEntity {
    @Id private UUID id;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @Column(name = "account_number", nullable = false, length = 20) private String accountNumber;
    @Column(nullable = false, length = 220) private String label;
    @Column(name = "account_class", nullable = false, length = 2) private String accountClass;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AccountNature nature;
    @Column(nullable = false) private boolean active;
    @Column(name = "system_account", nullable = false) private boolean systemAccount;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AccountingAccountEntity() { }
    public AccountingAccountEntity(UUID id, UUID hospitalId, String hospitalCode, String accountNumber, String label,
            String accountClass, AccountNature nature, boolean systemAccount, Instant createdAt) {
        this.id = id; this.hospitalId = hospitalId; this.hospitalCode = hospitalCode; this.accountNumber = accountNumber;
        this.label = label; this.accountClass = accountClass; this.nature = nature; this.active = true;
        this.systemAccount = systemAccount; this.createdAt = createdAt;
    }
    public UUID getId() { return id; }
    public UUID getHospitalId() { return hospitalId; }
    public String getHospitalCode() { return hospitalCode; }
    public String getAccountNumber() { return accountNumber; }
    public String getLabel() { return label; }
    public String getAccountClass() { return accountClass; }
    public AccountNature getNature() { return nature; }
    public boolean isActive() { return active; }
    public boolean isSystemAccount() { return systemAccount; }
    public Instant getCreatedAt() { return createdAt; }
    public void update(String label, AccountNature nature, boolean active) { this.label = label; this.nature = nature; this.active = active; }
}
