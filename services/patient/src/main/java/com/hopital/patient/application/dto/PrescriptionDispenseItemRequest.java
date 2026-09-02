package com.hopital.patient.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record PrescriptionDispenseItemRequest(
        @NotNull(message = "Le médicament délivré est obligatoire.") UUID prescriptionItemId,
        @NotBlank(message = "La quantité délivrée est obligatoire.")
        @Size(max = 100, message = "La quantité délivrée est trop longue.") String dispensedQuantity) {
}
