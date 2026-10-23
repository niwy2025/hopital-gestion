package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PartnerCompanyDocumentType;
import java.time.Instant;
import java.util.UUID;

public record PartnerCompanyDocumentResponse(
        UUID id,
        UUID partnerCompanyId,
        PartnerCompanyDocumentType documentType,
        String fileName,
        String contentType,
        int sizeBytes,
        Instant createdAt,
        String createdByUsername,
        String contentBase64) {
}
