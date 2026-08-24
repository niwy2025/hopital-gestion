package com.hopital.auth.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hospital.auth")
public record AuthServiceProperties(
        String issuerUri,
        String keycloakBaseUrl,
        String keycloakRealm,
        String keycloakClientId,
        String keycloakClientSecret) {
}
