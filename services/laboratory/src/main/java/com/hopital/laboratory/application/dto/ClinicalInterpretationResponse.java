package com.hopital.laboratory.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClinicalInterpretationResponse(
        UUID id,
        String resultCode,
        String clinicalConclusion,
        String doctorUsername,
        Instant interpretedAt,
        List<DiseaseResponse> diseases) {
}
