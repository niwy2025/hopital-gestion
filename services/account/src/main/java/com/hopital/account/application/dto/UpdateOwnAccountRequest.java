package com.hopital.account.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Personal profile update. Operational access data (username, roles and
 * hospital) is intentionally absent and can only be managed administratively.
 */
public record UpdateOwnAccountRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 255) String displayName,
        @Size(max = 255) String currentPassword,
        @Size(min = 8, max = 255) String newPassword,
        @Size(max = 700000) String profilePhotoBase64,
        @Size(max = 100) String profilePhotoContentType,
        boolean removeProfilePhoto) {
}
