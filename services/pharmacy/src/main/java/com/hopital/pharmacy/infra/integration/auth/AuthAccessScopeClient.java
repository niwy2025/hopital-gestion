package com.hopital.pharmacy.infra.integration.auth;

import com.hopital.pharmacy.application.domain.DataAccessScope;
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
        return new DataAccessScope(response.provinceWide(), response.administrator(), response.hospitalId(), response.hospitalCode());
    }
    private record Scope(boolean provinceWide, boolean administrator, UUID hospitalId, String hospitalCode) { }
}
