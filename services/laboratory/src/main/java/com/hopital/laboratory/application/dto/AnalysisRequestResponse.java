package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.LaboratoryType;
import java.time.Instant;
import java.util.UUID;

public record AnalysisRequestResponse(
        UUID id,
        String code,
        LaboratoryType laboratoryType,
        String laboratoryCode,
        String patientReference,
        String patientName,
        String analysisCode,
        String analysisName,
        String requesterName,
        AnalysisRequestStatus status,
        Instant createdAt) {
}
