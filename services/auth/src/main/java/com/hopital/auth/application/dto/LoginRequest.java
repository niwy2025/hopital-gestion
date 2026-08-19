package com.hopital.auth.application.dto;

public record LoginRequest(String identifier, String username, String email, String password) {

    public String resolvedIdentifier() {
        if (identifier != null && !identifier.isBlank()) {
            return identifier;
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        return username;
    }
}
