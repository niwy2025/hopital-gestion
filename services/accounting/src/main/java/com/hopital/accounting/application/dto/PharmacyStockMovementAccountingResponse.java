package com.hopital.accounting.application.dto;

import java.util.UUID;

/** Idempotent acknowledgement sent back to pharmacy-service. */
public record PharmacyStockMovementAccountingResponse(
        UUID accountingEntryId,
        String accountingEntryReference,
        String accountingEntryCode,
        boolean alreadyRecorded,
        boolean ignored,
        String ignoredReason) {
}
