package com.hopital.pharmacy.application.dto;

import com.hopital.pharmacy.application.domain.AccountingPostingStatus;
import com.hopital.pharmacy.application.domain.Currency;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StockEntryResponse(
        UUID id,
        String code,
        UUID hospitalId,
        String hospitalCode,
        UUID medicineId,
        String medicineCode,
        String genericName,
        int quantity,
        BigDecimal unitCost,
        BigDecimal unitSellingPrice,
        BigDecimal totalCost,
        Currency currency,
        LocalDate expiresOn,
        String supplierName,
        String notes,
        AccountingPostingStatus accountingStatus,
        Instant receivedAt,
        String receivedByUsername) {
}
