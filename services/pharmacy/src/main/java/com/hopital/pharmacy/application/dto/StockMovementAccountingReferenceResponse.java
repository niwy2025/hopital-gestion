package com.hopital.pharmacy.application.dto;

import com.hopital.pharmacy.application.domain.Currency;
import com.hopital.pharmacy.application.domain.StockMovementSourceType;
import com.hopital.pharmacy.application.domain.StockMovementType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Immutable source values consumed by accounting for one stock-out movement. */
public record StockMovementAccountingReferenceResponse(
        UUID stockMovementId,
        String stockMovementCode,
        StockMovementType type,
        StockMovementSourceType sourceType,
        String sourceCode,
        UUID hospitalId,
        String hospitalCode,
        int quantity,
        BigDecimal unitCost,
        BigDecimal totalCost,
        Currency currency,
        String notes,
        Instant occurredAt,
        String performedByUserId,
        String performedByUsername) {
}
