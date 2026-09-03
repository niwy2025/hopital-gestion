package com.hopital.auth.infra.integration.keycloak;

import com.hopital.auth.application.config.AuthServiceProperties;
import com.hopital.auth.application.dto.AccountResponse;
import com.hopital.auth.application.dto.RoleResponse;
import com.hopital.auth.application.exception.AuthException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KeycloakAuthClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakAuthClient.class);

    /**
     * Roles that are owned by account-service and therefore mirrored to Keycloak.
     * Keeping this list explicit prevents the authentication bridge from altering
     * Keycloak technical or realm-management roles.
     */
    private static final Set<String> MANAGED_ROLE_CODES = Set.of(
            "ADMIN",
            "HOSPITAL_ADMIN",
            "DOCTOR",
            "NURSE",
            "RECEPTIONIST",
            "PATIENT",
            "HR_MANAGER",
            "LABORATORY_TECHNICIAN",
            "LABORATORY_BIOLOGIST",
            "PHARMACIST",
            "PHARMACY_ADMIN",
            // Migration V11 remplace ce rôle ; il reste géré pour retirer son ancien mapping Keycloak.
            "PHARMACY_MANAGER",
            "BILLING_OFFICER",
            "CASHIER",
            "HOSPITAL_ACCOUNTANT",
            "FINANCE_MANAGER",
            "FINANCE_AUDITOR");

    private final RestClient keycloakClient;
    private final AuthServiceProperties properties;

    public KeycloakAuthClient(RestClient.Builder builder, AuthServiceProperties properties) {
        this.keycloakClient = builder.baseUrl(properties.keycloakBaseUrl()).build();
        this.properties = properties;
    }

    /**
     * The account service remains the source of truth for credentials.  Keycloak is kept in sync
     * only after the account service has validated the password.
     */
    public KeycloakToken login(AccountResponse account, String password) {
        try {
            String serviceAccessToken = requestServiceAccessToken();
            String keycloakUserId = findOrCreateUser(serviceAccessToken, account);
            synchronizeProfile(serviceAccessToken, keycloakUserId, account);
            resetPassword(serviceAccessToken, keycloakUserId, password);
            synchronizeRoles(serviceAccessToken, keycloakUserId, account.roles());
            return passwordGrant(account.username(), password);
        } catch (RestClientException exception) {
            LOGGER.warn("Échec de l'échange avec Keycloak pendant la connexion : {}", exception.getMessage());
            throw new AuthException("La connexion est momentanément indisponible.");
        }
    }

    public KeycloakToken refresh(String refreshToken) {
        try {
            MultiValueMap<String, String> form = clientForm();
            form.add("grant_type", "refresh_token");
            form.add("refresh_token", refreshToken);
            return toToken(requestToken(form));
        } catch (RestClientException exception) {
            LOGGER.warn("Échec du renouvellement de session auprès de Keycloak : {}", exception.getMessage());
            throw new AuthException("La session est invalide ou expirée.");
        }
    }

    private String requestServiceAccessToken() {
        MultiValueMap<String, String> form = clientForm();
        form.add("grant_type", "client_credentials");
        KeycloakTokenResponse response = requestToken(form);
        if (response.access_token() == null || response.access_token().isBlank()) {
            throw new AuthException("La connexion est momentanément indisponible.");
        }
        return response.access_token();
    }

    private String findOrCreateUser(String serviceAccessToken, AccountResponse account) {
        KeycloakUser[] users = keycloakClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/realms/{realm}/users")
                        .queryParam("username", account.username())
                        .queryParam("exact", true)
                        .build(properties.keycloakRealm()))
                .headers(headers -> headers.setBearerAuth(serviceAccessToken))
                .retrieve()
                .body(KeycloakUser[].class);

        if (users != null && users.length > 0) {
            return users[0].id();
        }

        ResponseEntity<Void> response = keycloakClient.post()
                .uri("/admin/realms/{realm}/users", properties.keycloakRealm())
                .headers(headers -> headers.setBearerAuth(serviceAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(userRepresentation(account))
                .retrieve()
                .toBodilessEntity();

        URI location = response.getHeaders().getLocation();
        if (location == null || location.getPath() == null) {
            throw new AuthException("La connexion est momentanément indisponible.");
        }
        String locationPath = location.getPath();
        return locationPath.substring(locationPath.lastIndexOf('/') + 1);
    }

    private void synchronizeProfile(String serviceAccessToken, String userId, AccountResponse account) {
        keycloakClient.put()
                .uri("/admin/realms/{realm}/users/{userId}", properties.keycloakRealm(), userId)
                .headers(headers -> headers.setBearerAuth(serviceAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(userRepresentation(account))
                .retrieve()
                .toBodilessEntity();
    }

    private Map<String, Object> userRepresentation(AccountResponse account) {
        String displayName = account.displayName() == null || account.displayName().isBlank()
                ? account.username()
                : account.displayName().trim();
        int firstSpace = displayName.indexOf(' ');
        String firstName = firstSpace < 0 ? displayName : displayName.substring(0, firstSpace);
        String lastName = firstSpace < 0 ? displayName : displayName.substring(firstSpace + 1).trim();
        return Map.of(
                "username", account.username(),
                "email", account.email(),
                "firstName", firstName,
                "lastName", lastName.isBlank() ? displayName : lastName,
                "enabled", true,
                "emailVerified", true,
                "requiredActions", List.of());
    }

    private void resetPassword(String serviceAccessToken, String userId, String password) {
        keycloakClient.put()
                .uri("/admin/realms/{realm}/users/{userId}/reset-password", properties.keycloakRealm(), userId)
                .headers(headers -> headers.setBearerAuth(serviceAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("type", "password", "value", password, "temporary", false))
                .retrieve()
                .toBodilessEntity();
    }

    private void synchronizeRoles(String serviceAccessToken, String userId, Set<RoleResponse> accountRoles) {
        Set<String> requestedRoles = accountRoles == null
                ? Set.of()
                : accountRoles.stream()
                        .map(RoleResponse::code)
                        .filter(MANAGED_ROLE_CODES::contains)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        KeycloakRole[] allRoles = keycloakClient.get()
                .uri("/admin/realms/{realm}/roles", properties.keycloakRealm())
                .headers(headers -> headers.setBearerAuth(serviceAccessToken))
                .retrieve()
                .body(KeycloakRole[].class);
        Map<String, KeycloakRole> realmRoles = Arrays.stream(allRoles == null ? new KeycloakRole[0] : allRoles)
                .collect(Collectors.toMap(KeycloakRole::name, role -> role));

        List<KeycloakRole> expectedRoles = requestedRoles.stream()
                .map(realmRoles::get)
                .filter(role -> role != null)
                .toList();
        if (expectedRoles.size() != requestedRoles.size()) {
            throw new AuthException("La configuration des rôles est incomplète.");
        }

        KeycloakRole[] assigned = keycloakClient.get()
                .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", properties.keycloakRealm(), userId)
                .headers(headers -> headers.setBearerAuth(serviceAccessToken))
                .retrieve()
                .body(KeycloakRole[].class);
        List<KeycloakRole> assignedRoles = Arrays.asList(assigned == null ? new KeycloakRole[0] : assigned);
        Set<String> assignedRoleNames = assignedRoles.stream().map(KeycloakRole::name).collect(Collectors.toSet());

        List<KeycloakRole> rolesToRemove = assignedRoles.stream()
                .filter(role -> MANAGED_ROLE_CODES.contains(role.name()))
                .filter(role -> !requestedRoles.contains(role.name()))
                .toList();
        if (!rolesToRemove.isEmpty()) {
            updateRoleMappings(HttpMethod.DELETE, serviceAccessToken, userId, rolesToRemove);
        }

        List<KeycloakRole> rolesToAdd = expectedRoles.stream()
                .filter(role -> !assignedRoleNames.contains(role.name()))
                .toList();
        if (!rolesToAdd.isEmpty()) {
            updateRoleMappings(HttpMethod.POST, serviceAccessToken, userId, rolesToAdd);
        }
    }

    private void updateRoleMappings(
            HttpMethod method, String serviceAccessToken, String userId, Collection<KeycloakRole> roles) {
        keycloakClient.method(method)
                .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", properties.keycloakRealm(), userId)
                .headers(headers -> headers.setBearerAuth(serviceAccessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(roles)
                .retrieve()
                .toBodilessEntity();
    }

    private KeycloakToken passwordGrant(String username, String password) {
        MultiValueMap<String, String> form = clientForm();
        form.add("grant_type", "password");
        form.add("username", username);
        form.add("password", password);
        return toToken(requestToken(form));
    }

    private MultiValueMap<String, String> clientForm() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.keycloakClientId());
        form.add("client_secret", properties.keycloakClientSecret());
        return form;
    }

    private KeycloakTokenResponse requestToken(MultiValueMap<String, String> form) {
        return keycloakClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", properties.keycloakRealm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KeycloakTokenResponse.class);
    }

    private KeycloakToken toToken(KeycloakTokenResponse response) {
        if (response == null
                || response.access_token() == null
                || response.access_token().isBlank()
                || response.refresh_token() == null
                || response.refresh_token().isBlank()
                || response.expires_in() <= 0
                || response.refresh_expires_in() <= 0) {
            throw new AuthException("La connexion est momentanément indisponible.");
        }
        return new KeycloakToken(
                response.access_token(),
                response.refresh_token(),
                response.token_type(),
                response.expires_in(),
                response.refresh_expires_in());
    }

    public record KeycloakToken(
            String accessToken, String refreshToken, String tokenType, long expiresIn, long refreshExpiresIn) {
    }

    private record KeycloakTokenResponse(
            String access_token,
            String refresh_token,
            String token_type,
            long expires_in,
            long refresh_expires_in) {
    }

    private record KeycloakUser(String id) {
    }

    private record KeycloakRole(String id, String name, String description, boolean composite, boolean clientRole) {
    }
}
