package com.hopital.accounting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountingEntryLineResponse(UUID id, int lineNumber, UUID accountId, String accountNumber, String accountLabel,
        String label, BigDecimal debit, BigDecimal credit, String thirdPartyReference) { }
