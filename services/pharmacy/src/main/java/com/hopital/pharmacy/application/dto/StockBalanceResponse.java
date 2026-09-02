package com.hopital.pharmacy.application.dto;

import com.hopital.pharmacy.application.domain.Currency;
import java.math.BigDecimal;
import java.time.Instant;
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
        int reorderLevel,
        BigDecimal averageUnitCost,
        Currency currency,
        boolean lowStock,
        Instant updatedAt) {
}
