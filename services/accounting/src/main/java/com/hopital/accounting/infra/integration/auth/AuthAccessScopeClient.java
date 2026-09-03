package com.hopital.accounting.infra.integration.auth;

import com.hopital.accounting.application.domain.DataAccessScope;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthAccessScopeClient {
    private final RestClient authClient;
    public AuthAccessScopeClient(RestClient.Builder builder, @Value("${hospital.auth-service.base-url}") String baseUrl) {
        authClient = builder.baseUrl(baseUrl).build();
    }
    public DataAccessScope resolve(String username) {
        Scope response = authClient.get().uri("/internal/auth/access-scopes/{username}", username).retrieve().body(Scope.class);
        if (response == null) throw new IllegalStateException("Le périmètre d'accès est indisponible.");
        return new DataAccessScope(response.provinceWide(), response.administrator(), parseId(response.hospitalId()), response.hospitalCode());
    }
    private UUID parseId(String value) { return value == null || value.isBlank() ? null : UUID.fromString(value); }
    private record Scope(boolean provinceWide, boolean administrator, String hospitalId, String hospitalCode) { }
}
