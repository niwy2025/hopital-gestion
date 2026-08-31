package com.hopital.auth.application.exception;

/**
 * Stable, client-safe reasons returned by the authentication API.
 */
public enum AuthFailureCode {
    INVALID_CREDENTIALS,
    ACCOUNT_ASSIGNMENT_REQUIRED,
    ACCESS_SCOPE_UNAVAILABLE,
    SESSION_INVALID,
    AUTHENTICATION_FAILED
}
