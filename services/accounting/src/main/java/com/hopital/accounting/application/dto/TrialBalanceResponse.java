package com.hopital.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TrialBalanceResponse(UUID hospitalId, UUID periodId, LocalDate dateFrom, LocalDate dateTo,
        BigDecimal totalDebit, BigDecimal totalCredit, List<TrialBalanceLineResponse> lines) { }
