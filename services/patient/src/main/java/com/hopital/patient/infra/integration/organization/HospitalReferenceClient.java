package com.hopital.patient.infra.integration.organization;

import com.hopital.patient.application.exception.InvalidRegistrationHospitalException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class HospitalReferenceClient {

    private final RestClient organizationClient;

    public HospitalReferenceClient(
            RestClient.Builder builder,
            @Value("${hospital.organization-service.base-url}") String organizationServiceBaseUrl) {
        this.organizationClient = builder.baseUrl(organizationServiceBaseUrl).build();
    }

    public HospitalReference resolveActiveHospital(UUID hospitalId) {
        try {
            HospitalReference reference = organizationClient.get()
                    .uri("/internal/organizations/hospitals/{hospitalId}/access-reference", hospitalId)
                    .retrieve()
                    .body(HospitalReference.class);
            if (reference == null || !reference.active()) {
                throw new InvalidRegistrationHospitalException(hospitalId);
            }
            return reference;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new InvalidRegistrationHospitalException(hospitalId);
            }
            throw exception;
        }
    }

    public record HospitalReference(UUID hospitalId, String hospitalCode, boolean active) {
    }
}
