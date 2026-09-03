package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingPaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccountingPaymentResponse(UUID id, UUID hospitalId, String hospitalCode, String code, UUID invoiceId,
        String invoiceCode, LocalDate paidOn, BigDecimal amount, AccountingCurrency currency, AccountingPaymentMethod method,
        String paymentReference, UUID accountingEntryId, String accountingEntryCode, Instant createdAt,
        String receivedByUsername) { }
