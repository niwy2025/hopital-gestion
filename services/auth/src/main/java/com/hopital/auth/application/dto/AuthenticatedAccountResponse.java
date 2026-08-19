package com.hopital.auth.application.dto;

public record AuthenticatedAccountResponse(boolean authenticated, AccountResponse account) {
}
