package com.hopital.patient.infra.integration.personnel;

import com.hopital.patient.application.exception.InvalidResponsiblePersonnelException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Resolves a personnel snapshot after checking the active hospital assignment. */
@Component
public class PersonnelReferenceClient {

    private final RestClient personnelClient;

    public PersonnelReferenceClient(
            RestClient.Builder builder,
            @Value("${hospital.personnel-service.base-url}") String personnelServiceBaseUrl) {
        this.personnelClient = builder.baseUrl(personnelServiceBaseUrl).build();
    }

    public PersonnelReference resolveActivePersonnelForHospital(UUID personnelId, UUID hospitalId) {
        try {
            PersonnelReference reference = personnelClient.get()
                    .uri("/internal/personnel/{personnelId}/hospitals/{hospitalId}/care-reference", personnelId, hospitalId)
                    .retrieve()
                    .body(PersonnelReference.class);
            if (reference == null) {
                throw new InvalidResponsiblePersonnelException("Le personnel responsable est introuvable.");
            }
            return reference;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new InvalidResponsiblePersonnelException(
                        "Le personnel sélectionné n’est pas actif ou n’est pas affecté à cet hôpital.");
            }
            throw exception;
        }
    }

    public record PersonnelReference(
            UUID id,
            String employeeNumber,
            String firstName,
            String lastName,
            String middleName,
            String category,
            String jobTitle) {
    }
}
