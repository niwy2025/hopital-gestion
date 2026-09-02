package com.hopital.pharmacy.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Internal contract called by patient-service once a pharmacy dispense is validated. */
public record PrescriptionStockDispenseRequest(
        @NotNull UUID hospitalId,
        @NotBlank @Size(max = 30) String dispenseCode,
        @NotBlank @Size(max = 100) String actorId,
        @NotBlank @Size(max = 150) String actorUsername,
        @NotEmpty @Size(max = 30) List<@Valid PrescriptionStockDispenseItemRequest> items) {
}
