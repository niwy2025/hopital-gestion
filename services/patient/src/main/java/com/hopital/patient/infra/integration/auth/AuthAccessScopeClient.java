package com.hopital.patient.infra.integration.auth;

import com.hopital.patient.application.domain.DataAccessScope;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthAccessScopeClient {

    private final RestClient authClient;

    public AuthAccessScopeClient(RestClient.Builder builder, @Value("${hospital.auth-service.base-url}") String authServiceBaseUrl) {
        this.authClient = builder.baseUrl(authServiceBaseUrl).build();
    }

    public DataAccessScope resolve(String username) {
        AuthAccessScopeResponse response = authClient.get()
                .uri("/internal/auth/access-scopes/{username}", username)
                .retrieve()
                .body(AuthAccessScopeResponse.class);
        if (response == null) {
            throw new IllegalStateException("Le périmètre d'accès est indisponible.");
        }
        return new DataAccessScope(
                response.provinceWide(),
                response.administrator(),
                response.personnelId(),
                response.hospitalId(),
                response.hospitalCode());
    }

    private record AuthAccessScopeResponse(
            boolean provinceWide,
            boolean administrator,
            UUID personnelId,
            UUID hospitalId,
            String hospitalCode) {
    }
}
