package com.hopital.account.application.dto;

import jakarta.validation.constraints.Size;

/**
 * Internal contract used by the personnel service to synchronize an agent's hospital perimeter.
 */
public record UpdateAccountHospitalAssignmentRequest(
        @Size(max = 36, message = "La référence de l’hôpital est invalide.") String hospitalId) {
}
