package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountNature;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateAccountingAccountRequest(UUID hospitalId,
        @NotBlank @Pattern(regexp = "[0-9A-Za-z._-]{1,20}", message = "Le numéro de compte est invalide.") String accountNumber,
        @NotBlank @Size(max = 220) String label, @NotNull AccountNature nature) { }
