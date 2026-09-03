package com.hopital.accounting.infra.integration.patient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Reads the immutable payment context of a pharmacy dispensing action. */
@Component
public class PatientAccountingReferenceClient {
    private final RestClient patientClient;
    public PatientAccountingReferenceClient(RestClient.Builder builder, @Value("${hospital.patient-service.base-url}") String baseUrl) {
        patientClient = builder.baseUrl(baseUrl).build();
    }
    public PharmacyDispenseReference resolve(String dispenseCode) {
        PharmacyDispenseReference response = patientClient.get()
                .uri("/internal/patients/pharmacy-dispensations/{dispenseCode}/accounting-reference", dispenseCode)
                .retrieve().body(PharmacyDispenseReference.class);
        if (response == null) throw new IllegalStateException("La référence patient de la délivrance est indisponible.");
        return response;
    }
    public record PharmacyDispenseReference(UUID dispenseId, String dispenseCode, UUID hospitalId, String hospitalCode,
            UUID patientId, String patientCode, UUID passageId, String passageCode, UUID prescriptionId,
            String prescriptionCode, String prescriptionSource, String completion, BigDecimal totalAmount,
            BigDecimal paidAmount, String currency, String paymentMethod, Instant dispensedAt,
            String dispensedByUserId, String dispensedByUsername) { }
}
