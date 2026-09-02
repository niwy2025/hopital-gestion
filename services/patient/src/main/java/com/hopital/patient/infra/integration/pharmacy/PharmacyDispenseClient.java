package com.hopital.patient.infra.integration.pharmacy;

import com.hopital.patient.application.domain.AuditActor;
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

    public void recordDispense(
            UUID hospitalId,
            String dispenseCode,
            AuditActor actor,
            List<StockDispenseItem> items) {
        if (items.isEmpty()) {
            return;
        }
        restClient.post()
                .uri("/internal/pharmacy/prescription-dispensations")
                .body(new PharmacyDispenseRequest(
                        hospitalId,
                        dispenseCode,
                        actor.userId(),
                        actor.username(),
                        items))
                .retrieve()
                .toBodilessEntity();
    }

    public record StockDispenseItem(UUID medicineId, int quantity) {
    }

    private record PharmacyDispenseRequest(
            UUID hospitalId,
            String dispenseCode,
            String actorId,
            String actorUsername,
            List<StockDispenseItem> items) {
    }
}
