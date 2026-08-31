package com.hopital.patient.application.dto;

import java.util.List;

/** Potential matches visible in the caller's access scope. */
public record PatientDuplicateCheckResponse(List<PatientSummaryResponse> matches) {
}
