package com.hopital.organization.application.dto;

import com.hopital.organization.application.domain.LaboratoryStructureType;
import java.util.UUID;

public record LaboratoryStructureResponse(
        UUID id,
        String code,
        String name,
        LaboratoryStructureType type,
        String referenceLaboratoryCode,
        boolean active) {
}
