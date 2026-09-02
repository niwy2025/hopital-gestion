package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.AnalysisPriority;
import com.hopital.laboratory.application.domain.LaboratoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Input for a request created from a patient care episode. */
public record CreatePatientPassageAnalysisRequest(
        @NotNull LaboratoryType laboratoryType,
        @NotBlank @Size(max = 30) String laboratoryCode,
        @NotBlank @Size(max = 200) String analysisName,
        @NotNull AnalysisPriority priority,
        @Size(max = 1000) String clinicalIndication) {

    /** Compatibility input for existing internal-laboratory callers. */
    public CreatePatientPassageAnalysisRequest(String laboratoryCode, String analysisName) {
        this(LaboratoryType.HOSPITAL, laboratoryCode, analysisName, AnalysisPriority.ROUTINE, null);
    }
}
