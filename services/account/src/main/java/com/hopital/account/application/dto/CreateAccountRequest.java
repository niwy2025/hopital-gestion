package com.hopital.account.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreateAccountRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 255) String displayName,
        @NotBlank @Size(min = 8, max = 255) String password,
        String hospitalId,
        @Size(max = 700000) String profilePhotoBase64,
        @Size(max = 100) String profilePhotoContentType,
        @NotEmpty Set<String> roles) {
}
