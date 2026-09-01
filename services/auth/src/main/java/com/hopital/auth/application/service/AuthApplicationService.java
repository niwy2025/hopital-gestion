package com.hopital.auth.application.service;

import com.hopital.auth.application.dto.AuthenticatedAccountResponse;
import com.hopital.auth.application.dto.AccountResponse;
import com.hopital.auth.application.dto.AccountWorkspaceResponse;
import com.hopital.auth.application.dto.DataAccessScopeResponse;
import com.hopital.auth.application.dto.HospitalAccessReferenceResponse;
import com.hopital.auth.application.dto.PersonnelAccessScopeResponse;
import com.hopital.auth.application.dto.LoginRequest;
import com.hopital.auth.application.dto.LoginResponse;
import com.hopital.auth.application.dto.RefreshTokenRequest;
import com.hopital.auth.application.exception.AuthException;
import com.hopital.auth.application.exception.AuthFailureCode;
import com.hopital.auth.infra.audit.LoginAuditRepository;
import com.hopital.auth.infra.integration.account.AccountClient;
import com.hopital.auth.infra.integration.keycloak.KeycloakAuthClient;
import com.hopital.auth.infra.integration.personnel.PersonnelAccessClient;
import com.hopital.auth.infra.integration.organization.OrganizationAccessClient;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestClientResponseException;

@Service
public class AuthApplicationService {

    private final KeycloakAuthClient keycloakAuthClient;
    private final AccountClient accountClient;
    private final PersonnelAccessClient personnelAccessClient;
    private final OrganizationAccessClient organizationAccessClient;
    private final LoginAuditRepository loginAuditRepository;
    private final JwtDecoder jwtDecoder;

    public AuthApplicationService(
            KeycloakAuthClient keycloakAuthClient,
            AccountClient accountClient,
            PersonnelAccessClient personnelAccessClient,
            OrganizationAccessClient organizationAccessClient,
            LoginAuditRepository loginAuditRepository,
            JwtDecoder jwtDecoder) {
        this.keycloakAuthClient = keycloakAuthClient;
        this.accountClient = accountClient;
        this.personnelAccessClient = personnelAccessClient;
        this.organizationAccessClient = organizationAccessClient;
        this.loginAuditRepository = loginAuditRepository;
        this.jwtDecoder = jwtDecoder;
    }

    public LoginResponse login(LoginRequest request) {
        AuthenticatedAccountResponse validatedAccount = accountClient.validateCredentials(
                new com.hopital.auth.application.dto.CredentialsValidationRequest(request.username(), request.password()));
        if (!validatedAccount.authenticated() || validatedAccount.account() == null) {
            loginAuditRepository.recordFailure(request.username(), request.userAgent());
            throw new AuthException(AuthFailureCode.INVALID_CREDENTIALS, "Identifiants invalides.");
        }

        assertCanSignInWithinAssignment(validatedAccount.account());

        KeycloakAuthClient.KeycloakToken token = keycloakAuthClient.login(validatedAccount.account(), request.password());
        LoginResponse response = toLoginResponse(token, request.userAgent());
        loginAuditRepository.recordSuccess(validatedAccount.account(), request.userAgent(), response.expiresAt());
        return response;
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new AuthException(AuthFailureCode.SESSION_INVALID, "Session invalide.");
        }
        KeycloakAuthClient.KeycloakToken token = keycloakAuthClient.refresh(request.refreshToken());
        String username = jwtDecoder.decode(token.accessToken()).getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            throw new AuthException(AuthFailureCode.SESSION_INVALID, "Session invalide.");
        }
        assertCanSignInWithinAssignment(accountClient.findByIdentifier(username));
        return toLoginResponse(token, request.userAgent());
    }

    public AccountWorkspaceResponse getAccountWorkspace(String username, String currentUserAgent) {
        if (username == null || username.isBlank()) {
            throw new AuthException(AuthFailureCode.SESSION_INVALID, "Session invalide.");
        }
        var account = accountClient.findByIdentifier(username);
        return new AccountWorkspaceResponse(account, loginAuditRepository.findKnownDevices(account.id(), currentUserAgent));
    }

    public DataAccessScopeResponse resolveDataAccessScope(String username) {
        if (username == null || username.isBlank()) {
            throw new AuthException("Session invalide.");
        }
        AccountResponse account = accountClient.findByIdentifier(username);
        if (isCentralAdministrator(account)) {
            return DataAccessScopeResponse.provinceWideAdministratorScope();
        }
        PersonnelAccessScopeResponse personnelScope = personnelAccessClient.resolveActiveScope(account.id());
        if ("PROVINCIAL".equals(personnelScope.scope())) {
            return DataAccessScopeResponse.provinceWidePersonnelScope(personnelScope.personnelId());
        }
        HospitalAccessReferenceResponse hospital = organizationAccessClient.resolveHospital(personnelScope.hospitalId());
        if ("HOSPITAL_LABORATORY".equals(personnelScope.scope())) {
            if (personnelScope.laboratoryCode() == null
                    || !hospital.hospitalLaboratoryCodes().contains(personnelScope.laboratoryCode())) {
                throw new AuthException("Le laboratoire de l’affectation est invalide ou inactif.");
            }
            return new DataAccessScopeResponse(
                    false,
                    false,
                    personnelScope.personnelId(),
                    hospital.hospitalId(),
                    hospital.hospitalCode(),
                    List.of(personnelScope.laboratoryCode()),
                    personnelScope.laboratoryCode());
        }
        return new DataAccessScopeResponse(
                false,
                false,
                personnelScope.personnelId(),
                hospital.hospitalId(),
                hospital.hospitalCode(),
                hospital.hospitalLaboratoryCodes(),
                null);
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

    private void assertCanSignInWithinAssignment(AccountResponse account) {
        if (isCentralAdministrator(account)) {
            return;
        }
        try {
            resolveDataAccessScope(account.username());
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AuthException(
                        AuthFailureCode.ACCOUNT_ASSIGNMENT_REQUIRED,
                        "Votre compte doit encore recevoir une affectation principale avant de pouvoir se connecter.");
            }
            throw new AuthException(
                    AuthFailureCode.ACCESS_SCOPE_UNAVAILABLE,
                    "Votre périmètre d’accès ne peut pas être vérifié pour le moment.");
        }
    }

    private boolean isCentralAdministrator(AccountResponse account) {
        return account.roles() != null && account.roles().stream().anyMatch(role -> "ADMIN".equals(role.code()));
    }
}
