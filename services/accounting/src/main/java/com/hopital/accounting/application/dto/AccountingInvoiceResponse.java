package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingSourceType;
import com.hopital.accounting.application.domain.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccountingInvoiceResponse(UUID id, UUID hospitalId, String hospitalCode, String code, AccountingSourceType sourceType,
        String sourceCode, UUID patientId, String patientCode, UUID passageId, String passageCode, LocalDate issuedOn,
        InvoiceStatus status, AccountingCurrency currency, BigDecimal totalAmount, BigDecimal paidAmount, BigDecimal dueAmount,
        String description, Instant createdAt, String createdByUsername) { }
