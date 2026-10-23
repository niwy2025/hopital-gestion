package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PartnerCompanyDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePartnerCompanyDocumentRequest(
        @NotNull PartnerCompanyDocumentType documentType,
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 120) String contentType,
        @NotBlank String contentBase64) {
}
