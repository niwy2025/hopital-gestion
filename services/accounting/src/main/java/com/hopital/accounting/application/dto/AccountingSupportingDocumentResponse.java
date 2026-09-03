package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.SupportingDocumentType;
import java.time.Instant;
import java.util.UUID;

public record AccountingSupportingDocumentResponse(UUID id, String relatedType, UUID relatedId, SupportingDocumentType type,
        String fileName, String contentType, long sizeBytes, Instant uploadedAt, String uploadedByUsername) { }
