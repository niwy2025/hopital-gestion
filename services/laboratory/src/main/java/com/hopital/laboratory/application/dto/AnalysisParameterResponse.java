package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.AnalysisValueType;
import java.util.List;
import java.util.UUID;

public record AnalysisParameterResponse(
        UUID id,
        String code,
        String name,
        AnalysisValueType valueType,
        String unit,
        String referenceRange,
        List<String> qualitativeOptions,
        boolean required,
        int displayOrder) {
}
