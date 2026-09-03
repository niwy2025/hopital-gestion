package com.hopital.patient.infra.integration.pharmacy;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.PaymentCurrency;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Internal patient-to-pharmacy call; never exposed to the browser or gateway. */
@Component
public class PharmacyDispenseClient {
    private final RestClient restClient;

    public PharmacyDispenseClient(
            RestClient.Builder builder,
            @Value("${hospital.pharmacy-service.base-url}") String pharmacyServiceBaseUrl) {
        this.restClient = builder.baseUrl(pharmacyServiceBaseUrl).build();
    }

    public DispenseValuation recordDispense(
            UUID hospitalId,
            String dispenseCode,
            AuditActor actor,
            BigDecimal paidAmount,
            PaymentCurrency paymentCurrency,
            List<StockDispenseItem> items) {
        if (items.isEmpty()) {
            return new DispenseValuation(BigDecimal.ZERO, null);
        }
        PharmacyDispenseResponse response = restClient.post()
                .uri("/internal/pharmacy/prescription-dispensations")
                .body(new PharmacyDispenseRequest(
                        hospitalId,
                        dispenseCode,
                        actor.userId(),
                        actor.username(),
                        paidAmount,
                        paymentCurrency.name(),
                        items))
                .retrieve()
                .body(PharmacyDispenseResponse.class);
        if (response == null || response.totalAmount() == null) {
            throw new IllegalStateException("La pharmacie n'a pas retourné le montant facturé de la délivrance.");
        }
        return new DispenseValuation(response.totalAmount(), response.currency());
    }

    public record StockDispenseItem(UUID medicineId, int quantity) {
    }

    public record DispenseValuation(BigDecimal totalAmount, String currency) {
    }

    private record PharmacyDispenseRequest(
            UUID hospitalId,
            String dispenseCode,
            String actorId,
            String actorUsername,
            BigDecimal paidAmount,
            String paymentCurrency,
            List<StockDispenseItem> items) {
    }

    private record PharmacyDispenseResponse(BigDecimal totalAmount, String currency) {
    }
}
