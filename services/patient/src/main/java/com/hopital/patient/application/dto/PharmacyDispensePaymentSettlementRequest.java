package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PaymentCurrency;
import com.hopital.patient.application.domain.PharmacyDispenseAccountingEventType;
import com.hopital.patient.application.domain.PharmacyDispenseAccountingInvoiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Immutable accounting snapshot delivered internally after an invoice is
 * issued or a payment is recorded. Amounts are cumulative invoice amounts,
 * never merely the value of the last payment.
 */
public record PharmacyDispensePaymentSettlementRequest(
        @NotNull UUID eventId,
        UUID paymentId,
        @NotNull UUID invoiceId,
        @NotBlank @Size(max = 80) String invoiceCode,
        @NotNull @DecimalMin(value = "0.00") BigDecimal totalAmount,
        @NotNull @DecimalMin(value = "0.00") BigDecimal paidAmount,
        @NotNull @DecimalMin(value = "0.00") BigDecimal dueAmount,
        @NotNull PaymentCurrency currency,
        @NotNull PharmacyDispenseAccountingInvoiceStatus status,
        LocalDate paidOn,
        @Size(max = 160) String paymentReference,
        @NotNull PharmacyDispenseAccountingEventType eventType,
        @NotNull @PositiveOrZero Long stateVersion) {
}
