package com.hopital.auth.application.service;

import com.hopital.auth.application.dto.AccountResponse;
import com.hopital.auth.application.dto.AuthenticatedAccountResponse;
import com.hopital.auth.application.dto.CredentialsValidationRequest;
import com.hopital.auth.application.dto.LoginRequest;
import com.hopital.auth.application.dto.LoginResponse;
import com.hopital.auth.application.dto.RoleResponse;
import com.hopital.auth.application.exception.AuthException;
import com.hopital.auth.infra.integration.account.AccountClient;
import com.hopital.auth.infra.integration.keycloak.KeycloakAuthClient;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

    private final AccountClient accountClient;
    private final KeycloakAuthClient keycloakAuthClient;

    public AuthApplicationService(AccountClient accountClient, KeycloakAuthClient keycloakAuthClient) {
        this.accountClient = accountClient;
        this.keycloakAuthClient = keycloakAuthClient;
    }

    public LoginResponse login(LoginRequest request) {
        AuthenticatedAccountResponse authentication = accountClient.validateCredentials(
                new CredentialsValidationRequest(request.resolvedIdentifier(), request.password()));
        if (authentication == null || !authentication.authenticated()) {
            throw new AuthException("Invalid username/email or password");
        }
        AccountResponse account = authentication.account();
        Set<String> roles = account.roles().stream().map(RoleResponse::code).collect(Collectors.toUnmodifiableSet());
        Set<String> permissions = account.roles().stream()
                .flatMap(role -> role.permissions().stream())
                .map(permission -> permission.code())
                .collect(Collectors.toUnmodifiableSet());
        return new LoginResponse(
                keycloakAuthClient.issueAccessToken(account),
                "Bearer",
                account.id(),
                account.username(),
                account.email(),
                roles,
                permissions);
    }
}
