package com.hopital.auth.infra.integration.keycloak;

import com.hopital.auth.application.dto.AccountResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class KeycloakAuthClient {

    public String issueAccessToken(AccountResponse account) {
        String tokenPayload = account.id() + ":" + account.username() + ":" + Instant.now();
        return Base64.getUrlEncoder().encodeToString(tokenPayload.getBytes(StandardCharsets.UTF_8));
    }
}
