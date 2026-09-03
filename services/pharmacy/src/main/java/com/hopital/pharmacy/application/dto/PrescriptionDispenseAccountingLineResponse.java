package com.hopital.pharmacy.application.dto;

import com.hopital.pharmacy.application.domain.Currency;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** One costed stock issue belonging to a pharmacy dispensation. */
public record PrescriptionDispenseAccountingLineResponse(
        UUID stockMovementId,
        String stockMovementCode,
        UUID medicineId,
        String medicineCode,
        String medicineName,
        int quantity,
        BigDecimal unitCost,
        BigDecimal totalCost,
        Currency currency,
        Instant occurredAt) {
}
