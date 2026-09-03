package com.hopital.accounting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Management view derived only from posted entries; statutory validation remains the accountant's responsibility. */
public record FinancialStatementsResponse(UUID hospitalId, UUID periodId, Instant generatedAt,
        BigDecimal totalDebit, BigDecimal totalCredit, BigDecimal result, List<FinancialStatementSectionResponse> balanceSheet,
        List<FinancialStatementSectionResponse> incomeStatement, List<FinancialStatementSectionResponse> cashFlowStatement,
        long financialStatementNotesCount) { }
