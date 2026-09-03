package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.SupportingDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UploadAccountingSupportingDocumentRequest(@NotNull SupportingDocumentType type,
        @NotBlank @Size(max = 255) String fileName, @NotBlank @Size(max = 120) String contentType,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9+/=\\r\\n]+$", message = "Le contenu du justificatif doit être encodé en base64.")
        @Size(max = 4194304, message = "Le justificatif encodé ne doit pas dépasser 4 Mo.") String contentBase64) { }
