package com.hopital.patient.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PartnerCompanyResponse(
        UUID id,
        UUID hospitalId,
        String code,
        String name,
        String legalName,
        String phoneNumber,
        String email,
        String address,
        boolean active,
        Instant createdAt,
        String createdByUsername,
        Instant updatedAt,
        String updatedByUsername) {
}
