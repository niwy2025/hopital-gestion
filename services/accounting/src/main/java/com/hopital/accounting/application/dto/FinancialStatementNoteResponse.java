package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.FinancialStatementNoteStatus;
import com.hopital.accounting.application.domain.FinancialStatementNoteType;
import java.time.Instant;
import java.util.UUID;

public record FinancialStatementNoteResponse(UUID id, UUID hospitalId, String hospitalCode, UUID periodId, String code,
        String title, FinancialStatementNoteType type, String content, FinancialStatementNoteStatus status, Instant createdAt,
        String createdByUsername, Instant validatedAt, String validatedByUsername) { }
