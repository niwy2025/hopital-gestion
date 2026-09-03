package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountingCurrency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateAccountingInvoiceRequest(UUID hospitalId, UUID patientId, @Size(max = 50) String patientCode,
        UUID passageId, @Size(max = 50) String passageCode, @NotNull LocalDate issuedOn,
        @NotNull @DecimalMin("0.00") @Digits(integer = 14, fraction = 2) BigDecimal totalAmount,
        @NotNull AccountingCurrency currency, @NotBlank @Size(max = 1000) String description) { }
