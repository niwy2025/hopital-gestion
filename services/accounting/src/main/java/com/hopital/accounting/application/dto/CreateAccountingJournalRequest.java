package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.JournalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateAccountingJournalRequest(UUID hospitalId,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,20}", message = "Le code journal est invalide.") String code,
        @NotBlank @Size(max = 180) String label, @NotNull JournalType type) { }
