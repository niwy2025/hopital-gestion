package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PaymentCurrency;
import com.hopital.patient.application.domain.PrescriptionPaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreatePrescriptionDispenseRequest(
        boolean complete,
        @NotNull(message = "Le montant payé est obligatoire.")
        @DecimalMin(value = "0.00", message = "Le montant payé ne peut pas être négatif.")
        @Digits(integer = 12, fraction = 2, message = "Le montant payé doit comporter au plus deux décimales.")
        BigDecimal paidAmount,
        @NotNull(message = "La devise du paiement est obligatoire.") PaymentCurrency currency,
        @NotNull(message = "Le mode de paiement est obligatoire.") PrescriptionPaymentMethod paymentMethod,
        @Size(max = 4000, message = "La note de délivrance est trop longue.") String notes,
        @NotEmpty(message = "Sélectionnez au moins un médicament délivré.")
        @Size(max = 30, message = "Une délivrance ne peut pas contenir plus de 30 médicaments.")
        List<@Valid PrescriptionDispenseItemRequest> items) {
}
