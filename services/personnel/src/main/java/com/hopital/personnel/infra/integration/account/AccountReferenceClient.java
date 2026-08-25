package com.hopital.personnel.infra.integration.account;

import com.hopital.personnel.application.exception.InvalidPersonnelReferenceException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
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

    public void assertAccountExists(UUID accountId) {
        try {
            accountClient.get()
                    .uri("/internal/accounts/{accountId}", accountId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new InvalidPersonnelReferenceException("Le compte utilisateur sélectionné est introuvable.");
                    })
                    .toBodilessEntity();
        } catch (InvalidPersonnelReferenceException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw new InvalidPersonnelReferenceException("Le compte utilisateur sélectionné est introuvable.");
        } catch (RuntimeException exception) {
            throw new InvalidPersonnelReferenceException("Le compte utilisateur ne peut pas être vérifié pour le moment.");
        }
    }
}
