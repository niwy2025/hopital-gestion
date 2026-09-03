package com.hopital.accounting.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountingEntryLineRequest(@NotNull UUID accountId, @Size(max = 1000) String label,
        @NotNull @DecimalMin("0.00") @Digits(integer = 14, fraction = 2) BigDecimal debit,
        @NotNull @DecimalMin("0.00") @Digits(integer = 14, fraction = 2) BigDecimal credit,
        @Size(max = 100) String thirdPartyReference) { }
