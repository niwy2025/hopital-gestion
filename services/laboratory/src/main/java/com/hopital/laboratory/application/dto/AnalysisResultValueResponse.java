package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.AnalysisResultFlag;
import com.hopital.laboratory.application.domain.AnalysisValueType;
import java.util.UUID;

public record AnalysisResultValueResponse(
        UUID requestParameterId,
        String code,
        String name,
        AnalysisValueType valueType,
        String value,
        String unit,
        String referenceRange,
        AnalysisResultFlag abnormalFlag,
        String comment) {
}
