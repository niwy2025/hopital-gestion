package com.hopital.auth.application.service;

import com.hopital.auth.application.dto.LoginRequest;
import com.hopital.auth.application.dto.LoginResponse;
import com.hopital.auth.infra.integration.keycloak.KeycloakAuthClient;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

    private final KeycloakAuthClient keycloakAuthClient;

    public AuthApplicationService(KeycloakAuthClient keycloakAuthClient) {
        this.keycloakAuthClient = keycloakAuthClient;
    }

    public LoginResponse login(LoginRequest request) {
        return keycloakAuthClient.login(request);
    }
}
