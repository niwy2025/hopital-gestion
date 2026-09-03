package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountNature;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAccountingAccountRequest(@NotBlank @Size(max = 220) String label, @NotNull AccountNature nature, boolean active) { }
