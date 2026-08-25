package com.hopital.account.application.dto;

import java.util.Set;

/**
 * Detailed representation, deliberately reserved for one account at a time because profile
 * photos must not be loaded in the paginated account register.
 */
public record AccountDetailsResponse(
        String id,
        String username,
        String email,
        String displayName,
        String hospitalId,
        Set<RoleResponse> roles,
        String profilePhotoBase64,
        String profilePhotoContentType) {
}
