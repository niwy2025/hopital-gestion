package com.hopital.auth.application.dto;

public record CredentialsValidationRequest(String identifier, String password) {
}
