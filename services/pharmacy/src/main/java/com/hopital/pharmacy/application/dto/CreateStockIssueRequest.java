package com.hopital.pharmacy.application.dto;

import com.hopital.pharmacy.application.domain.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateStockIssueRequest(
        @NotNull(message = "Le médicament est obligatoire.") UUID medicineId,
        @NotNull(message = "Le type de sortie est obligatoire.") StockMovementType type,
        @NotNull(message = "La quantité est obligatoire.") @Min(value = 1, message = "La quantité doit être positive.") Integer quantity,
        @Size(max = 2000) String notes) { }
