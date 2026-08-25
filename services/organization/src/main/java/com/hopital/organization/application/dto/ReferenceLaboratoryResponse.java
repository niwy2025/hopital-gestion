package com.hopital.organization.application.dto;

import java.util.UUID;

public record ReferenceLaboratoryResponse(
        UUID id,
        String code,
        String name,
        String provinceCode,
        String address,
        String phoneNumber,
        boolean active) {
}
