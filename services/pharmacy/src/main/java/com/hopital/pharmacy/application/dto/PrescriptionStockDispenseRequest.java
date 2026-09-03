package com.hopital.pharmacy.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Internal contract called by patient-service once a pharmacy dispense is validated. */
public record PrescriptionStockDispenseRequest(
        @NotNull UUID hospitalId,
        @NotBlank @Size(max = 30) String dispenseCode,
        @NotBlank @Size(max = 100) String actorId,
        @NotBlank @Size(max = 150) String actorUsername,
        @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) java.math.BigDecimal paidAmount,
        @NotBlank @Pattern(regexp = "CDF|USD") String paymentCurrency,
        @NotEmpty @Size(max = 30) List<@Valid PrescriptionStockDispenseItemRequest> items) {
}
