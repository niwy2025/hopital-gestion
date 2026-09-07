package com.hopital.patient.application.dto;

import com.hopital.patient.application.domain.TriageConsciousnessLevel;
import com.hopital.patient.application.domain.TriageDangerSign;
import com.hopital.patient.application.domain.TriagePregnancyStatus;
import com.hopital.patient.application.domain.TriagePriority;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

/** A dated triage assessment appended to an open patient passage. */
public record CreatePatientPassageTriageAssessmentRequest(
        @NotBlank(message = "Le motif de triage est obligatoire.")
        @Size(max = 500, message = "Le motif de triage est trop long.")
        String chiefComplaint,
        boolean injury,
        @NotNull(message = "Le niveau de priorité est obligatoire.") TriagePriority priority,
        @NotBlank(message = "La justification de la priorité est obligatoire.")
        @Size(max = 1000, message = "La justification de la priorité est trop longue.")
        String priorityReason,
        @Min(value = 1, message = "La fréquence cardiaque doit être positive.")
        @Max(value = 400, message = "La fréquence cardiaque est invalide.")
        Integer heartRateBpm,
        @Min(value = 1, message = "La fréquence respiratoire doit être positive.")
        @Max(value = 200, message = "La fréquence respiratoire est invalide.")
        Integer respiratoryRatePerMinute,
        @Min(value = 1, message = "La pression systolique doit être positive.")
        @Max(value = 350, message = "La pression systolique est invalide.")
        Integer systolicBloodPressure,
        @Min(value = 1, message = "La pression diastolique doit être positive.")
        @Max(value = 250, message = "La pression diastolique est invalide.")
        Integer diastolicBloodPressure,
        @DecimalMin(value = "25.0", message = "La température est invalide.")
        @DecimalMax(value = "45.0", message = "La température est invalide.")
        BigDecimal temperatureCelsius,
        @Min(value = 0, message = "La saturation doit être comprise entre 0 et 100.")
        @Max(value = 100, message = "La saturation doit être comprise entre 0 et 100.")
        Integer oxygenSaturationPercent,
        @Min(value = 0, message = "La glycémie ne peut pas être négative.")
        @Max(value = 1500, message = "La glycémie est invalide.")
        Integer randomBloodGlucoseMgDl,
        @Min(value = 0, message = "Le score de douleur doit être compris entre 0 et 10.")
        @Max(value = 10, message = "Le score de douleur doit être compris entre 0 et 10.")
        Integer painScore,
        @DecimalMin(value = "0.1", message = "Le poids doit être positif.")
        @DecimalMax(value = "500.0", message = "Le poids est invalide.")
        BigDecimal weightKg,
        TriageConsciousnessLevel consciousnessLevel,
        TriagePregnancyStatus pregnancyStatus,
        @Min(value = 0, message = "Le temps de recoloration capillaire ne peut pas être négatif.")
        @Max(value = 60, message = "Le temps de recoloration capillaire est invalide.")
        Integer capillaryRefillSeconds,
        @Size(max = 200, message = "Le dispositif d’oxygène est trop long.") String oxygenSupport,
        @DecimalMin(value = "0.0", message = "Le débit d’oxygène ne peut pas être négatif.")
        @DecimalMax(value = "100.0", message = "Le débit d’oxygène est invalide.")
        BigDecimal oxygenFlowLitersPerMinute,
        @Size(max = 20, message = "Trop de signes d’alerte ont été sélectionnés.") Set<TriageDangerSign> dangerSigns,
        @Size(max = 4000, message = "Les soins à l’arrivée sont trop longs.") String careOnArrival,
        @Size(max = 4000, message = "Les observations de transmission sont trop longues.") String handoverNotes) {
}
