package com.hopital.patient.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.hopital.patient.application.domain.AccountingSynchronizationStatus;
import com.hopital.patient.application.domain.PaymentCurrency;
import com.hopital.patient.application.domain.PharmacyDispenseAccountingInvoiceStatus;
import com.hopital.patient.application.domain.PrescriptionDispenseCompletion;
import com.hopital.patient.application.domain.PrescriptionPaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PrescriptionDispenseResponse(
        UUID id,
        String code,
        PrescriptionDispenseCompletion completion,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal dueAmount,
        PaymentCurrency currency,
        PrescriptionPaymentMethod paymentMethod,
        @JsonAlias("accountingSynchronizationStatus") AccountingSynchronizationStatus accountingSyncStatus,
        UUID accountingInvoiceId,
        String accountingInvoiceCode,
        BigDecimal accountingTotalAmount,
        BigDecimal accountingPaidAmount,
        BigDecimal accountingDueAmount,
        PaymentCurrency accountingCurrency,
        @JsonAlias("accountingStatus") PharmacyDispenseAccountingInvoiceStatus accountingInvoiceStatus,
        Long accountingStateVersion,
        Instant accountingSynchronizedAt,
        String accountingLastPaymentReference,
        String notes,
        Instant dispensedAt,
        String dispensedByUsername,
        List<PrescriptionDispenseItemResponse> items) {
}
