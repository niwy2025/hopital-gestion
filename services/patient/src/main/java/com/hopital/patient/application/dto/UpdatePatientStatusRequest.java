package com.hopital.patient.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePatientStatusRequest(@NotNull Boolean active) {
}
