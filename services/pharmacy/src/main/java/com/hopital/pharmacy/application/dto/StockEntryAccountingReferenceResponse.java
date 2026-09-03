package com.hopital.pharmacy.application.dto;

import com.hopital.pharmacy.application.domain.Currency;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Immutable financial reference reread by accounting from a stock receipt. */
public record StockEntryAccountingReferenceResponse(
        UUID stockEntryId,
        String stockEntryCode,
        UUID hospitalId,
        String hospitalCode,
        String supplierName,
        BigDecimal totalCost,
        Currency currency,
        Instant receivedAt,
        String receivedByUserId,
        String receivedByUsername) {
}
