package com.hopital.laboratory.infra.integration.auth;

import com.hopital.laboratory.application.domain.DataAccessScope;
import java.util.UUID;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
                parseHospitalId(response.hospitalId()),
                response.hospitalCode(),
                response.hospitalLaboratoryCodes() == null
                        ? Set.of()
                        : new LinkedHashSet<>(response.hospitalLaboratoryCodes()));
    }

    private record AuthAccessScopeResponse(
            boolean provinceWide,
            boolean administrator,
            String hospitalId,
            String hospitalCode,
            List<String> hospitalLaboratoryCodes) {
    }

    private UUID parseHospitalId(String hospitalId) {
        return hospitalId == null || hospitalId.isBlank() ? null : UUID.fromString(hospitalId);
    }
}
