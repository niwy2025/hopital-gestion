package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.AnalysisResultFlag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateAnalysisResultValueRequest(
        @NotNull UUID requestParameterId,
        @NotBlank @Size(max = 2000) String value,
        AnalysisResultFlag abnormalFlag,
        @Size(max = 1000) String comment) {
}
