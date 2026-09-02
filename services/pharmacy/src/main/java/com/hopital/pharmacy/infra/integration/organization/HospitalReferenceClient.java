package com.hopital.pharmacy.infra.integration.organization;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HospitalReferenceClient {
    private final RestClient organizationClient;
    public HospitalReferenceClient(RestClient.Builder builder, @Value("${hospital.organization-service.base-url}") String baseUrl) {
        organizationClient = builder.baseUrl(baseUrl).build();
    }
    public HospitalReference resolveActive(UUID hospitalId) {
        HospitalReference reference = organizationClient.get().uri("/internal/organizations/hospitals/{hospitalId}/access-reference", hospitalId).retrieve().body(HospitalReference.class);
        if (reference == null || !reference.active()) throw new IllegalArgumentException("L’hôpital sélectionné n’est pas actif.");
        return reference;
    }
    public record HospitalReference(UUID hospitalId, String hospitalCode, boolean active) { }
}
