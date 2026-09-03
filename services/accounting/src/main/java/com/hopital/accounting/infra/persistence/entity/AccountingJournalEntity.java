package com.hopital.accounting.infra.persistence.entity;

import com.hopital.accounting.application.domain.JournalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounting_journals")
public class AccountingJournalEntity {
    @Id private UUID id;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @Column(nullable = false, length = 20) private String code;
    @Column(nullable = false, length = 180) private String label;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private JournalType type;
    @Column(nullable = false) private boolean active;
    @Column(name = "system_journal", nullable = false) private boolean systemJournal;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AccountingJournalEntity() { }
    public AccountingJournalEntity(UUID id, UUID hospitalId, String hospitalCode, String code, String label,
            JournalType type, boolean systemJournal, Instant createdAt) {
        this.id = id; this.hospitalId = hospitalId; this.hospitalCode = hospitalCode; this.code = code;
        this.label = label; this.type = type; this.active = true; this.systemJournal = systemJournal; this.createdAt = createdAt;
    }
    public UUID getId() { return id; } public UUID getHospitalId() { return hospitalId; } public String getHospitalCode() { return hospitalCode; }
    public String getCode() { return code; } public String getLabel() { return label; } public JournalType getType() { return type; }
    public boolean isActive() { return active; } public boolean isSystemJournal() { return systemJournal; } public Instant getCreatedAt() { return createdAt; }
    public void update(String label, JournalType type, boolean active) { this.label = label; this.type = type; this.active = active; }
}
