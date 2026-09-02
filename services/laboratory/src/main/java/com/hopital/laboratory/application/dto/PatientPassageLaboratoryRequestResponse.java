package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.AnalysisPriority;
import com.hopital.laboratory.application.domain.AnalysisResultStatus;
import com.hopital.laboratory.application.domain.LaboratoryType;
import com.hopital.laboratory.application.domain.SpecimenStatus;
import com.hopital.laboratory.application.domain.SpecimenType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Consolidated laboratory timeline for one analysis request of a passage. */
public record PatientPassageLaboratoryRequestResponse(
        UUID id,
        UUID patientPassageId,
        String code,
        LaboratoryType laboratoryType,
        String laboratoryCode,
        String analysisCode,
        String analysisName,
        String requesterName,
        AnalysisPriority priority,
        String clinicalIndication,
        AnalysisRequestStatus status,
        Instant createdAt,
        List<SpecimenTimelineResponse> specimens,
        ResultTimelineResponse result) {

    public record SpecimenTimelineResponse(
            String code,
            SpecimenType specimenType,
            SpecimenStatus status,
            Instant collectedAt,
            String collectedBy,
            Instant dispatchedAt,
            String dispatchedBy,
            String carrierName,
            Instant receivedAt,
            String receivedBy,
            String rejectionReason) {
    }

    public record ResultTimelineResponse(
            String code,
            String resultValue,
            String unit,
            String referenceRange,
            String comment,
            AnalysisResultStatus status,
            Instant enteredAt,
            Instant validatedAt,
            String validatedBy) {
    }
}
