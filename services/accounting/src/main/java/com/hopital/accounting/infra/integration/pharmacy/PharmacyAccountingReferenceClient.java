package com.hopital.accounting.infra.integration.pharmacy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Reads the immutable stock-cost side of a pharmacy dispensing action. */
@Component
public class PharmacyAccountingReferenceClient {
    private final RestClient pharmacyClient;
    public PharmacyAccountingReferenceClient(RestClient.Builder builder, @Value("${hospital.pharmacy-service.base-url}") String baseUrl) {
        pharmacyClient = builder.baseUrl(baseUrl).build();
    }
    public PharmacyDispenseStockReference resolve(String dispenseCode) {
        PharmacyDispenseStockReference response = pharmacyClient.get()
                .uri("/internal/pharmacy/prescription-dispensations/{dispenseCode}/accounting-reference", dispenseCode)
                .retrieve().body(PharmacyDispenseStockReference.class);
        if (response == null) throw new IllegalStateException("La référence de stock de la délivrance est indisponible.");
        return response;
    }

    /** Reads the immutable cost and supplier side of a stock receipt. */
    public PharmacyStockEntryReference resolveStockEntry(String stockEntryCode) {
        PharmacyStockEntryReference response = pharmacyClient.get()
                .uri("/internal/pharmacy/stock-entries/{stockEntryCode}/accounting-reference", stockEntryCode)
                .retrieve().body(PharmacyStockEntryReference.class);
        if (response == null) throw new IllegalStateException("La référence comptable de l'entrée de stock est indisponible.");
        return response;
    }

    /** Reads the immutable cost and classification of one pharmacy stock-out. */
    public PharmacyStockMovementReference resolveStockMovement(String stockMovementCode) {
        PharmacyStockMovementReference response = pharmacyClient.get()
                .uri("/internal/pharmacy/stock-movements/{stockMovementCode}/accounting-reference", stockMovementCode)
                .retrieve().body(PharmacyStockMovementReference.class);
        if (response == null) {
            throw new IllegalStateException("La référence comptable de la sortie de stock est indisponible.");
        }
        return response;
    }
    public record PharmacyDispenseStockReference(String dispenseCode, UUID hospitalId, String hospitalCode,
            BigDecimal totalCost, String currency, Instant occurredAt) { }

    public record PharmacyStockEntryReference(UUID stockEntryId, String stockEntryCode, UUID hospitalId,
            String hospitalCode, String supplierName, BigDecimal totalCost, String currency, Instant receivedAt,
            String receivedByUserId, String receivedByUsername) { }

    public record PharmacyStockMovementReference(UUID stockMovementId, String stockMovementCode, String type,
            String sourceType, String sourceCode, UUID hospitalId, String hospitalCode, int quantity,
            BigDecimal unitCost, BigDecimal totalCost, String currency, String notes, Instant occurredAt,
            String performedByUserId, String performedByUsername) { }
}
