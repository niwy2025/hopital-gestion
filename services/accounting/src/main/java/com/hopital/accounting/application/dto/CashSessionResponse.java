package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.CashSessionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashSessionResponse(UUID id, UUID hospitalId, String hospitalCode, String code, AccountingCurrency currency,
        BigDecimal openingAmount, CashSessionStatus status, Instant openedAt, String openedByUsername, Instant closedAt,
        String closedByUsername, BigDecimal expectedClosingAmount, BigDecimal declaredClosingAmount,
        BigDecimal varianceAmount, String closingNotes) { }
