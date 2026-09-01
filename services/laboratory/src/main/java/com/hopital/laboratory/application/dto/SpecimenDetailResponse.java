package com.hopital.laboratory.application.dto;

import java.util.List;

/** The selected specimen together with the complete workflow of its request. */
public record SpecimenDetailResponse(
        SpecimenResponse specimen,
        AnalysisRequestResponse request,
        List<SpecimenResponse> specimens,
        AnalysisResultResponse result) {
}
