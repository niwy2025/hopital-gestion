package com.hopital.account.application.dto;

public record AuthenticatedAccountResponse(boolean authenticated, AccountResponse account) {
}
