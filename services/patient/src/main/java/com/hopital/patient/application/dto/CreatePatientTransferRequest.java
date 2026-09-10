package com.hopital.patient.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreatePatientTransferRequest(
        UUID destinationHospitalId,
        @Size(max = 200) String externalFacilityName,
        @Size(max = 500) String externalFacilityAddress,
        @Size(max = 100) String destinationContact,
        @NotBlank @Size(max = 500) String reason,
        @NotBlank @Size(max = 8000) String clinicalSummary,
        @Size(max = 4000) String treatmentAndInstructions,
        @Size(max = 1000) String transportDetails,
        boolean urgent) {
}
