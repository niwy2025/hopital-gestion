package com.hopital.auth.application.service;

import com.hopital.auth.application.dto.AuthenticatedAccountResponse;
import com.hopital.auth.application.dto.LoginRequest;
import com.hopital.auth.application.dto.LoginResponse;
import com.hopital.auth.application.dto.RefreshTokenRequest;
import com.hopital.auth.application.exception.AuthException;
import com.hopital.auth.infra.audit.LoginAuditRepository;
import com.hopital.auth.infra.integration.account.AccountClient;
import com.hopital.auth.infra.integration.keycloak.KeycloakAuthClient;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

    private final KeycloakAuthClient keycloakAuthClient;
    private final AccountClient accountClient;
    private final LoginAuditRepository loginAuditRepository;

    public AuthApplicationService(
            KeycloakAuthClient keycloakAuthClient,
            AccountClient accountClient,
            LoginAuditRepository loginAuditRepository) {
        this.keycloakAuthClient = keycloakAuthClient;
        this.accountClient = accountClient;
        this.loginAuditRepository = loginAuditRepository;
    }

    public LoginResponse login(LoginRequest request) {
        AuthenticatedAccountResponse validatedAccount = accountClient.validateCredentials(
                new com.hopital.auth.application.dto.CredentialsValidationRequest(request.username(), request.password()));
        if (!validatedAccount.authenticated() || validatedAccount.account() == null) {
            loginAuditRepository.recordFailure(request.username(), request.userAgent());
            throw new AuthException("Identifiants invalides.");
        }

        KeycloakAuthClient.KeycloakToken token = keycloakAuthClient.login(validatedAccount.account(), request.password());
        LoginResponse response = toLoginResponse(token, request.userAgent());
        loginAuditRepository.recordSuccess(validatedAccount.account(), request.userAgent(), response.expiresAt());
        return response;
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new AuthException("Session invalide.");
        }
        return toLoginResponse(keycloakAuthClient.refresh(request.refreshToken()), request.userAgent());
    }

    private LoginResponse toLoginResponse(KeycloakAuthClient.KeycloakToken token, String userAgent) {
        Instant issuedAt = Instant.now();
        return new LoginResponse(
                token.accessToken(),
                token.refreshToken(),
                token.tokenType(),
                token.expiresIn(),
                issuedAt.plusSeconds(token.expiresIn()),
                token.refreshExpiresIn(),
                issuedAt.plusSeconds(token.refreshExpiresIn()),
                userAgent == null || userAgent.isBlank() ? "unknown" : userAgent);
    }
}
