package com.hopital.accounting.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CloseCashSessionRequest(@NotNull @DecimalMin("0.00") @Digits(integer = 14, fraction = 2) BigDecimal declaredClosingAmount,
        @Size(max = 2000) String notes) { }
