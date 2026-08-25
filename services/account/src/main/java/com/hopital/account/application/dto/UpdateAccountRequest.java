package com.hopital.account.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

/**
 * A blank password keeps the current password. When a password is supplied, it becomes the new
 * temporary password and must contain at least eight characters.
 */
public record UpdateAccountRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 255) String displayName,
        @Size(min = 8, max = 255) String password,
        String hospitalId,
        @Size(max = 700000) String profilePhotoBase64,
        @Size(max = 100) String profilePhotoContentType,
        boolean removeProfilePhoto,
        @NotEmpty Set<String> roles) {
}
