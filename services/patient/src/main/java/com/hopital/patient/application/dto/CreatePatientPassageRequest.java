package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PatientPassageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreatePatientPassageRequest(
        UUID hospitalId,
        @NotNull PatientPassageType type,
        @Size(max = 150) String serviceName,
        @Size(max = 500) String reason,
        UUID responsiblePersonnelId) {
}
