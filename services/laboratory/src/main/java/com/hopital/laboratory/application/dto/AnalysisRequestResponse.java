package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.AnalysisPriority;
import com.hopital.laboratory.application.domain.LaboratoryType;
import com.hopital.laboratory.application.domain.SpecimenType;
import java.time.Instant;
import java.util.List;
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
        UUID originHospitalId,
        String originHospitalCode,
        AnalysisPriority priority,
        String clinicalIndication,
        AnalysisRequestStatus status,
        Instant createdAt,
        UUID patientPassageId,
        UUID analysisDefinitionId,
        SpecimenType requestedSpecimenType,
        List<AnalysisParameterResponse> requestedParameters) {
}
