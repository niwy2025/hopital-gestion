package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.PatientPassageStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePatientPassageStatusRequest(@NotNull PatientPassageStatus status) {
}
