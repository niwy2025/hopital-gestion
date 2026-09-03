package com.hopital.accounting.infra.persistence.entity;

import com.hopital.accounting.application.domain.FinancialStatementNoteStatus;
import com.hopital.accounting.application.domain.FinancialStatementNoteType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** A period-scoped note annexed to the financial statements. */
@Entity
@Table(name = "financial_statement_notes")
public class FinancialStatementNoteEntity {
    @Id private UUID id;
    @Column(name = "hospital_id", nullable = false) private UUID hospitalId;
    @Column(name = "hospital_code", nullable = false, length = 30) private String hospitalCode;
    @Column(name = "period_id") private UUID periodId;
    @Column(nullable = false, length = 50) private String code;
    @Column(nullable = false, length = 240) private String title;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private FinancialStatementNoteType type;
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private FinancialStatementNoteStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by_user_id", nullable = false, length = 100) private String createdByUserId;
    @Column(name = "created_by_username", nullable = false, length = 150) private String createdByUsername;
    @Column(name = "validated_at") private Instant validatedAt;
    @Column(name = "validated_by_user_id", length = 100) private String validatedByUserId;
    @Column(name = "validated_by_username", length = 150) private String validatedByUsername;

    protected FinancialStatementNoteEntity() { }
    public FinancialStatementNoteEntity(UUID id, UUID hospitalId, String hospitalCode, UUID periodId, String code,
            String title, FinancialStatementNoteType type, String content, String userId, String username, Instant createdAt) {
        this.id = id; this.hospitalId = hospitalId; this.hospitalCode = hospitalCode; this.periodId = periodId; this.code = code;
        this.title = title; this.type = type; this.content = content; this.status = FinancialStatementNoteStatus.DRAFT;
        this.createdByUserId = userId; this.createdByUsername = username; this.createdAt = createdAt;
    }
    public UUID getId() { return id; } public UUID getHospitalId() { return hospitalId; } public String getHospitalCode() { return hospitalCode; }
    public UUID getPeriodId() { return periodId; } public String getCode() { return code; } public String getTitle() { return title; }
    public FinancialStatementNoteType getType() { return type; } public String getContent() { return content; }
    public FinancialStatementNoteStatus getStatus() { return status; } public Instant getCreatedAt() { return createdAt; }
    public String getCreatedByUsername() { return createdByUsername; } public Instant getValidatedAt() { return validatedAt; } public String getValidatedByUsername() { return validatedByUsername; }
    public void update(String title, FinancialStatementNoteType type, String content) {
        if (status == FinancialStatementNoteStatus.VALIDATED) throw new IllegalStateException("Une annexe validée est append-only.");
        this.title = title; this.type = type; this.content = content;
    }
    public void validate(String userId, String username, Instant at) { this.status = FinancialStatementNoteStatus.VALIDATED; this.validatedByUserId = userId; this.validatedByUsername = username; this.validatedAt = at; }
}
