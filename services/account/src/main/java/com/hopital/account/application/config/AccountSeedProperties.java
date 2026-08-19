package com.hopital.account.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hospital.account.seed")
public record AccountSeedProperties(String adminUsername, String adminEmail, String adminPassword) {
}
