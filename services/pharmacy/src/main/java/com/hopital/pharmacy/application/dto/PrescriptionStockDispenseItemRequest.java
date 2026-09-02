package com.hopital.pharmacy.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PrescriptionStockDispenseItemRequest(
        @NotNull(message = "Le médicament est obligatoire.") UUID medicineId,
        @Min(value = 1, message = "La quantité délivrée doit être positive.") int quantity) {
}
