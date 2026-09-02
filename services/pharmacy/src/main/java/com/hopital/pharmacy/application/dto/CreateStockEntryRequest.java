package com.hopital.pharmacy.application.dto;

import com.hopital.pharmacy.application.domain.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateStockEntryRequest(
        @NotNull(message = "Le médicament est obligatoire.") UUID medicineId,
        @NotNull(message = "La quantité reçue est obligatoire.") @Min(value = 1, message = "La quantité doit être positive.") Integer quantity,
        @NotNull(message = "Le coût unitaire est obligatoire.") @DecimalMin(value = "0.01", message = "Le coût unitaire doit être positif.") BigDecimal unitCost,
        @NotNull(message = "La monnaie est obligatoire.") Currency currency,
        @Min(value = 0, message = "Le seuil d’alerte ne peut pas être négatif.") Integer reorderLevel,
        LocalDate expiresOn,
        @Size(max = 200) String supplierName,
        @Size(max = 2000) String notes) {
}
