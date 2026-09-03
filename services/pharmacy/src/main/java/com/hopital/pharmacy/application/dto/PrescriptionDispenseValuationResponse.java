package com.hopital.pharmacy.application.dto;

import com.hopital.pharmacy.application.domain.Currency;
import java.math.BigDecimal;

/** Server-calculated selling value of one prescription delivery. */
public record PrescriptionDispenseValuationResponse(
        String dispenseCode,
        BigDecimal totalAmount,
        Currency currency) {
}
