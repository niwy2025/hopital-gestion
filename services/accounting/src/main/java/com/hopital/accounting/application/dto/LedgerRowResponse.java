package com.hopital.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LedgerRowResponse(UUID entryId, String entryCode, LocalDate entryDate, String journalCode, String description,
        UUID accountId, String accountNumber, String accountLabel, String label, BigDecimal debit, BigDecimal credit,
        String thirdPartyReference) { }
