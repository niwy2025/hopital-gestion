package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PatientPassageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReceivePatientTransferRequest(
        @NotNull PatientPassageType type,
        @Size(max = 150) String serviceName,
        @Size(max = 1000) String receptionNote) {
}
