package com.hopital.pharmacy.application.dto;

import com.hopital.pharmacy.application.domain.Currency;
import com.hopital.pharmacy.application.domain.StockMovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        String code,
        StockMovementType type,
        UUID stockId,
        UUID hospitalId,
        String hospitalCode,
        UUID medicineId,
        String medicineCode,
        String genericName,
        String lotCode,
        int quantity,
        BigDecimal unitCost,
        Currency currency,
        LocalDate expiresOn,
        String notes,
        Instant occurredAt,
        String performedByUsername) { }
