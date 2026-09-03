package com.hopital.accounting.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountingDashboardResponse(UUID hospitalId, String hospitalCode, long openInvoices,
        BigDecimal outstandingReceivables, long currentOpenPeriods, long postedEntries, long draftFinancialStatementNotes) { }
