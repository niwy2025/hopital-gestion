package com.hopital.laboratory.application.dto;

import java.util.List;

/** Complete laboratory workflow for a single analysis request. */
public record AnalysisRequestDetailResponse(
        AnalysisRequestResponse request,
        List<SpecimenResponse> specimens,
        AnalysisResultResponse result) {
}
