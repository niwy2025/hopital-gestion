package com.hopital.auth.infra.integration.keycloak;

import com.hopital.auth.application.dto.LoginRequest;
import com.hopital.auth.application.dto.LoginResponse;
import org.springframework.stereotype.Component;

@Component
public class KeycloakAuthClient {

    public LoginResponse login(LoginRequest request) {
        return new LoginResponse("replace-with-keycloak-token-exchange", "Bearer");
    }
}
