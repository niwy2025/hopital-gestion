package com.hopital.laboratory.infra.integration.organization;

import com.hopital.laboratory.application.exception.InvalidLaboratoryWorkflowException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Reads active internal laboratories for the hospital that owns a passage. */
@Component
public class HospitalLaboratoryReferenceClient {

    private final RestClient organizationClient;

    public HospitalLaboratoryReferenceClient(
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
                throw new InvalidLaboratoryWorkflowException("L'hôpital rattaché à ce passage n'est plus actif.");
            }
            return reference;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new InvalidLaboratoryWorkflowException("L'hôpital rattaché à ce passage est introuvable.");
            }
            throw exception;
        }
    }

    public record HospitalReference(
            UUID hospitalId,
            String hospitalCode,
            boolean active,
            List<String> hospitalLaboratoryCodes,
            List<HospitalLaboratoryReference> hospitalLaboratories) {
    }

    public record HospitalLaboratoryReference(String code, String name) {
    }
}
