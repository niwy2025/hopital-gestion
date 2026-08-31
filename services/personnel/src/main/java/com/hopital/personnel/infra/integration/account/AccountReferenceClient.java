package com.hopital.personnel.infra.integration.account;

import com.hopital.personnel.application.exception.InvalidPersonnelReferenceException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Verifies that an optional personnel-account association targets a real account,
 * while keeping the account database private to its owning service.
 */
@Component
public class AccountReferenceClient {

    private final RestClient accountClient;

    public AccountReferenceClient(
            RestClient.Builder builder,
            @Value("${personnel.account-service-url:http://account-service:8082}") String accountServiceUrl) {
        this.accountClient = builder.baseUrl(accountServiceUrl).build();
    }

    public AccountReference assertAccountExists(UUID accountId) {
        try {
            AccountReferencePayload payload = accountClient.get()
                    .uri("/internal/accounts/{accountId}", accountId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new InvalidPersonnelReferenceException("Le compte utilisateur sélectionné est introuvable.");
                    })
                    .body(AccountReferencePayload.class);
            if (payload == null || payload.id() == null || payload.id().isBlank()) {
                throw new InvalidPersonnelReferenceException("Le compte utilisateur ne peut pas être vérifié pour le moment.");
            }
            return new AccountReference(parseUuid(payload.id()), parseUuid(payload.hospitalId()));
        } catch (InvalidPersonnelReferenceException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw new InvalidPersonnelReferenceException("Le compte utilisateur sélectionné est introuvable.");
        } catch (RuntimeException exception) {
            throw new InvalidPersonnelReferenceException("Le compte utilisateur ne peut pas être vérifié pour le moment.");
        }
    }

    public void synchronizeHospitalAssignment(UUID accountId, UUID hospitalId) {
        try {
            accountClient.patch()
                    .uri("/internal/accounts/{accountId}/hospital-assignment", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new HospitalAssignmentPayload(hospitalId == null ? null : hospitalId.toString()))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new InvalidPersonnelReferenceException(
                                "L’hôpital du compte utilisateur ne peut pas être synchronisé pour le moment.");
                    })
                    .toBodilessEntity();
        } catch (InvalidPersonnelReferenceException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new InvalidPersonnelReferenceException(
                    "L’hôpital du compte utilisateur ne peut pas être synchronisé pour le moment.");
        }
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidPersonnelReferenceException("La référence du compte utilisateur est invalide.");
        }
    }

    public record AccountReference(UUID id, UUID hospitalId) {
    }

    private record AccountReferencePayload(String id, String hospitalId) {
    }

    private record HospitalAssignmentPayload(String hospitalId) {
    }
}
