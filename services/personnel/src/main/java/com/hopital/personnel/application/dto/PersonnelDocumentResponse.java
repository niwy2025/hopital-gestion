package com.hopital.personnel.application.dto;

import com.hopital.personnel.application.domain.PersonnelDocumentType;
import java.time.Instant;
import java.util.UUID;

public record PersonnelDocumentResponse(
        UUID id,
        PersonnelDocumentType documentType,
        String fileName,
        String contentType,
        int sizeBytes,
        Instant createdAt,
        String contentBase64) {
}
