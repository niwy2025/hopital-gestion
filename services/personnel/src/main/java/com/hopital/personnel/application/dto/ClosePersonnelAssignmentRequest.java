package com.hopital.personnel.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ClosePersonnelAssignmentRequest(
        @NotNull(message = "La date de fin est obligatoire.") LocalDate endsOn) {
}
