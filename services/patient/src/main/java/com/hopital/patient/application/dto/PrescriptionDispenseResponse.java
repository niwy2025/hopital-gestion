package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PaymentCurrency;
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
        BigDecimal paidAmount,
        PaymentCurrency currency,
        PrescriptionPaymentMethod paymentMethod,
        String notes,
        Instant dispensedAt,
        String dispensedByUsername,
        List<PrescriptionDispenseItemResponse> items) {
}
