package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.AnalysisResultStatus;
import java.time.Instant;
import java.util.UUID;

public record AnalysisResultResponse(
        UUID id,
        String code,
        String analysisRequestCode,
        String patientName,
        String analysisName,
        String resultValue,
        String unit,
        String referenceRange,
        String comment,
        AnalysisResultStatus status,
        Instant enteredAt,
        Instant validatedAt,
        String validatedBy) {
}
