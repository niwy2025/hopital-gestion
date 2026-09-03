package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.InvoiceStatus;
import java.util.UUID;

public record PharmacyDispensationAccountingResponse(UUID invoiceId, String invoiceReference, String invoiceCode,
        UUID accountingEntryId, String accountingEntryCode, InvoiceStatus status, boolean alreadyRecorded) { }
