package com.hopital.auth.infra.integration.account;

import com.hopital.auth.application.dto.AuthenticatedAccountResponse;
import com.hopital.auth.application.dto.CredentialsValidationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccountClient {

    private final RestClient restClient;

    public AccountClient(RestClient.Builder builder, @Value("${hospital.account-service.base-url}") String accountServiceBaseUrl) {
        this.restClient = builder.baseUrl(accountServiceBaseUrl).build();
    }

    public AuthenticatedAccountResponse validateCredentials(CredentialsValidationRequest request) {
        return restClient.post()
                .uri("/internal/accounts/validate-credentials")
                .body(request)
                .retrieve()
                .body(AuthenticatedAccountResponse.class);
    }
}
