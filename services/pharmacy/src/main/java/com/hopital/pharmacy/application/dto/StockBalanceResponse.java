package com.hopital.pharmacy.application.dto;

import com.hopital.pharmacy.application.domain.Currency;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StockBalanceResponse(
        UUID id,
        UUID hospitalId,
        String hospitalCode,
        UUID medicineId,
        String medicineCode,
        String genericName,
        String commercialName,
        String dosage,
        String pharmaceuticalForm,
        int quantity,
        int availableQuantity,
        int expiredQuantity,
        int expiringQuantity,
        LocalDate nearestExpiry,
        int reorderLevel,
        BigDecimal averageUnitCost,
        BigDecimal unitSellingPrice,
        Currency currency,
        boolean lowStock,
        Instant updatedAt) {
}
