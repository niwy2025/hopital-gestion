package com.hopital.personnel.infra.integration.organization;

import com.hopital.personnel.application.exception.InvalidPersonnelReferenceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Keeps reference-laboratory assignment validation in the organization
 * service, which owns that registry.
 */
@Component
public class ReferenceLaboratoryReferenceClient {

    private final RestClient organizationClient;

    public ReferenceLaboratoryReferenceClient(
            RestClient.Builder builder,
            @Value("${personnel.organization-service-url:http://organization-service:8084}") String organizationServiceUrl) {
        this.organizationClient = builder.baseUrl(organizationServiceUrl).build();
    }

    public ReferenceLaboratoryReference assertActiveReferenceLaboratory(String laboratoryCode) {
        try {
            ReferenceLaboratoryReference reference = organizationClient.get()
                    .uri("/internal/organizations/reference-laboratories/{laboratoryCode}/assignment-reference", laboratoryCode)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new InvalidPersonnelReferenceException(
                                "Le laboratoire de référence sélectionné est introuvable.");
                    })
                    .body(ReferenceLaboratoryReference.class);
            if (reference == null || reference.code() == null || reference.code().isBlank() || !reference.active()) {
                throw new InvalidPersonnelReferenceException(
                        "Le laboratoire de référence sélectionné est inactif ou indisponible.");
            }
            return reference;
        } catch (InvalidPersonnelReferenceException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new InvalidPersonnelReferenceException(
                    "Le laboratoire de référence sélectionné ne peut pas être vérifié pour le moment.");
        }
    }

    public record ReferenceLaboratoryReference(String code, boolean active) {
    }
}
