package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.AccountingSynchronizationStatus;
import com.hopital.patient.application.domain.PaymentCurrency;
import com.hopital.patient.application.domain.PharmacyDispenseAccountingInvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Result returned to accounting after an idempotent settlement projection. */
public record PharmacyDispensePaymentSettlementResponse(
        UUID dispenseId,
        String dispenseCode,
        boolean applied,
        AccountingSynchronizationStatus synchronizationStatus,
        UUID invoiceId,
        String invoiceCode,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal dueAmount,
        PaymentCurrency currency,
        PharmacyDispenseAccountingInvoiceStatus status,
        Long stateVersion,
        Instant synchronizedAt) {
}
