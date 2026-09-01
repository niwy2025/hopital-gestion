package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PatientDocumentType;
import java.time.Instant;
import java.util.UUID;

public record PatientDocumentResponse(
        UUID id,
        PatientDocumentType documentType,
        String fileName,
        String contentType,
        int sizeBytes,
        Instant createdAt,
        String createdByUsername,
        String contentBase64) {
}
