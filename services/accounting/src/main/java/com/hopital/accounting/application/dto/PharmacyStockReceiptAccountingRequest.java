package com.hopital.accounting.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Internal durable outbox event from pharmacy-service. */
public record PharmacyStockReceiptAccountingRequest(
        @NotBlank(message = "Le code de l'entrée de stock est obligatoire.") String stockEntryCode) {
}
