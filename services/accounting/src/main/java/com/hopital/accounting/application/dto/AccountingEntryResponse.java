package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingEntryStatus;
import com.hopital.accounting.application.domain.AccountingSourceType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AccountingEntryResponse(UUID id, UUID hospitalId, String hospitalCode, UUID periodId, UUID journalId,
        String journalCode, String code, AccountingSourceType sourceType, String sourceCode, LocalDate entryDate,
        String description, AccountingEntryStatus status, AccountingCurrency currency, BigDecimal totalDebit,
        BigDecimal totalCredit, Instant createdAt, String createdByUsername, Instant postedAt, String postedByUsername,
        UUID reversalEntryId, List<AccountingEntryLineResponse> lines) { }
