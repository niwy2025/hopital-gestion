package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountingCurrency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record OpenCashSessionRequest(UUID hospitalId, @NotNull AccountingCurrency currency,
        @NotNull @DecimalMin("0.00") @Digits(integer = 14, fraction = 2) BigDecimal openingAmount) { }
