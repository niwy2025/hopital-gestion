package com.hopital.patient.application.dto;

import java.util.UUID;

public record TriageCustomAlertSignResponse(
        UUID alertSignId,
        String code,
        String label) {
}
