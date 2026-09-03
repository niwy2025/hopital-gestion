package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountingCurrency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateAccountingEntryRequest(UUID hospitalId, @NotNull UUID journalId, @NotNull LocalDate entryDate,
        @NotBlank @Size(max = 1000) String description, @NotNull AccountingCurrency currency,
        @NotEmpty @Size(min = 2, max = 100) List<@Valid CreateAccountingEntryLineRequest> lines) { }
