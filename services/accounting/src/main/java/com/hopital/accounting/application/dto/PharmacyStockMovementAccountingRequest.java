package com.hopital.accounting.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Internal durable outbox event from pharmacy-service for a stock-out movement. */
public record PharmacyStockMovementAccountingRequest(
        @NotBlank(message = "Le code du mouvement de stock est obligatoire.") String stockMovementCode) {
}
