package com.hopital.laboratory.application.dto;

import com.hopital.laboratory.application.domain.AnalysisRequestEventType;
import java.time.Instant;

/** A read-only item in the traceability timeline of an analysis request. */
public record AnalysisRequestEventResponse(
        AnalysisRequestEventType type,
        String actorUsername,
        String note,
        Instant occurredAt) {
}
