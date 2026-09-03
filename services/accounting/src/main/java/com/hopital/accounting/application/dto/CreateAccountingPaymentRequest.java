package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingPaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateAccountingPaymentRequest(@NotNull LocalDate paidOn,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 14, fraction = 2) BigDecimal amount,
        @NotNull AccountingCurrency currency, @NotNull AccountingPaymentMethod method,
        @Size(max = 150) String paymentReference) { }
