package com.hopital.organization.application.dto;

import java.util.List;
import java.util.UUID;

/** Internal reference used to resolve an account's hospital data perimeter. */
public record HospitalAccessReferenceResponse(
        UUID hospitalId,
        String hospitalCode,
        boolean active,
        List<String> hospitalLaboratoryCodes) {
}
