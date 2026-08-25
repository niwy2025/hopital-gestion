package com.hopital.personnel.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePersonnelStatusRequest(@NotNull Boolean active) {
}
