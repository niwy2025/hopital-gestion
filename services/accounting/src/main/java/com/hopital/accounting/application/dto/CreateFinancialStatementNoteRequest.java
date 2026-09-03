package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.FinancialStatementNoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateFinancialStatementNoteRequest(UUID hospitalId, UUID periodId,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,50}", message = "Le code d'annexe est invalide.") String code,
        @NotBlank @Size(max = 240) String title, @NotNull FinancialStatementNoteType type,
        @NotBlank @Size(max = 50000) String content) { }
