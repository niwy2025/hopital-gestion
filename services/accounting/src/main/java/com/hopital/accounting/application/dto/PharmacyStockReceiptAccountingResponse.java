package com.hopital.accounting.application.dto;

import java.util.UUID;

/** Idempotent acknowledgement sent back to pharmacy-service. */
public record PharmacyStockReceiptAccountingResponse(
        UUID accountingEntryId,
        String accountingEntryReference,
        String accountingEntryCode,
        boolean alreadyRecorded) {
}
