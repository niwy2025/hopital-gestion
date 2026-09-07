package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.TriageConsciousnessLevel;
import com.hopital.patient.application.domain.TriageDangerSign;
import com.hopital.patient.application.domain.TriagePregnancyStatus;
import com.hopital.patient.application.domain.TriagePriority;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Immutable triage assessment returned in the passage history. */
public record PatientPassageTriageAssessmentResponse(
        UUID id,
        UUID passageId,
        String chiefComplaint,
        boolean injury,
        TriagePriority priority,
        String priorityReason,
        Integer heartRateBpm,
        Integer respiratoryRatePerMinute,
        Integer systolicBloodPressure,
        Integer diastolicBloodPressure,
        BigDecimal temperatureCelsius,
        Integer oxygenSaturationPercent,
        Integer randomBloodGlucoseMgDl,
        Integer painScore,
        BigDecimal weightKg,
        TriageConsciousnessLevel consciousnessLevel,
        TriagePregnancyStatus pregnancyStatus,
        Integer capillaryRefillSeconds,
        String oxygenSupport,
        BigDecimal oxygenFlowLitersPerMinute,
        Set<TriageDangerSign> dangerSigns,
        String careOnArrival,
        String handoverNotes,
        Instant recordedAt,
        String recordedByUsername) {
}
