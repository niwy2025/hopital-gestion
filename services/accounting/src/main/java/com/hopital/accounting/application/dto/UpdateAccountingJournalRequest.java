package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.JournalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAccountingJournalRequest(@NotBlank @Size(max = 180) String label, @NotNull JournalType type, boolean active) { }
