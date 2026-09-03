package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.FinancialStatementNoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFinancialStatementNoteRequest(@NotBlank @Size(max = 240) String title,
        @NotNull FinancialStatementNoteType type, @NotBlank @Size(max = 50000) String content) { }
