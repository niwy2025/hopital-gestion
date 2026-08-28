package com.hopital.auth.application.dto;

/**
 * Active data perimeter returned by the personnel service for a connected account.
 */
public record PersonnelAccessScopeResponse(
        String accountId,
        String personnelId,
        String scope,
        String hospitalId,
        String laboratoryCode) {
}
