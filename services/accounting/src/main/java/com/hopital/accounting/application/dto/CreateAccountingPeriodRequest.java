package com.hopital.accounting.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreateAccountingPeriodRequest(UUID hospitalId,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,40}", message = "Le code période est invalide.") String code,
        @NotBlank @Size(max = 180) String label, @NotNull LocalDate startsOn, @NotNull LocalDate endsOn) { }
