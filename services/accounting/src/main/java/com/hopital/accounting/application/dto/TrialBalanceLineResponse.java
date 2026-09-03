package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountNature;
import java.math.BigDecimal;
import java.util.UUID;

public record TrialBalanceLineResponse(UUID accountId, String accountNumber, String accountLabel, AccountNature nature,
        BigDecimal totalDebit, BigDecimal totalCredit, BigDecimal balanceDebit, BigDecimal balanceCredit) { }
