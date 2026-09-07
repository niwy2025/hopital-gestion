package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.TriageConsciousnessLevel;
import com.hopital.patient.application.domain.TriageDangerSign;
import com.hopital.patient.application.domain.TriagePregnancyStatus;
import com.hopital.patient.application.domain.TriagePriority;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Append-only initial assessment or re-assessment performed before the medical
 * consultation. Clinical priority remains a manual professional decision.
 */
@Entity
@Table(name = "patient_passage_triage_assessments")
public class PatientPassageTriageAssessmentEntity {

    @Id
    private UUID id;

    @Column(name = "passage_id", nullable = false)
    private UUID passageId;

    @Column(name = "chief_complaint", nullable = false, length = 500)
    private String chiefComplaint;

    @Column(nullable = false)
    private boolean injury;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TriagePriority priority;

    @Column(name = "priority_reason", nullable = false, length = 1000)
    private String priorityReason;

    @Column(name = "heart_rate_bpm")
    private Integer heartRateBpm;

    @Column(name = "respiratory_rate_per_minute")
    private Integer respiratoryRatePerMinute;

    @Column(name = "systolic_blood_pressure")
    private Integer systolicBloodPressure;

    @Column(name = "diastolic_blood_pressure")
    private Integer diastolicBloodPressure;

    @Column(name = "temperature_celsius", precision = 5, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(name = "oxygen_saturation_percent")
    private Integer oxygenSaturationPercent;

    @Column(name = "random_blood_glucose_mg_dl")
    private Integer randomBloodGlucoseMgDl;

    @Column(name = "pain_score")
    private Integer painScore;

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "consciousness_level", nullable = false, length = 20)
    private TriageConsciousnessLevel consciousnessLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "pregnancy_status", nullable = false, length = 20)
    private TriagePregnancyStatus pregnancyStatus;

    @Column(name = "capillary_refill_seconds")
    private Integer capillaryRefillSeconds;

    @Column(name = "oxygen_support", length = 200)
    private String oxygenSupport;

    @Column(name = "oxygen_flow_lpm", precision = 5, scale = 2)
    private BigDecimal oxygenFlowLitersPerMinute;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "patient_passage_triage_danger_signs",
            joinColumns = @JoinColumn(name = "triage_assessment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "danger_sign", nullable = false, length = 50)
    private Set<TriageDangerSign> dangerSigns = new LinkedHashSet<>();

    @Column(name = "care_on_arrival", columnDefinition = "TEXT")
    private String careOnArrival;

    @Column(name = "handover_notes", columnDefinition = "TEXT")
    private String handoverNotes;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "recorded_by_user_id", nullable = false, length = 100)
    private String recordedByUserId;

    @Column(name = "recorded_by_username", nullable = false, length = 150)
    private String recordedByUsername;

    protected PatientPassageTriageAssessmentEntity() {
    }

    public PatientPassageTriageAssessmentEntity(
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
            AuditActor actor,
            Instant recordedAt) {
        this.id = id;
        this.passageId = passageId;
        this.chiefComplaint = chiefComplaint;
        this.injury = injury;
        this.priority = priority;
        this.priorityReason = priorityReason;
        this.heartRateBpm = heartRateBpm;
        this.respiratoryRatePerMinute = respiratoryRatePerMinute;
        this.systolicBloodPressure = systolicBloodPressure;
        this.diastolicBloodPressure = diastolicBloodPressure;
        this.temperatureCelsius = temperatureCelsius;
        this.oxygenSaturationPercent = oxygenSaturationPercent;
        this.randomBloodGlucoseMgDl = randomBloodGlucoseMgDl;
        this.painScore = painScore;
        this.weightKg = weightKg;
        this.consciousnessLevel = consciousnessLevel;
        this.pregnancyStatus = pregnancyStatus;
        this.capillaryRefillSeconds = capillaryRefillSeconds;
        this.oxygenSupport = oxygenSupport;
        this.oxygenFlowLitersPerMinute = oxygenFlowLitersPerMinute;
        this.dangerSigns = dangerSigns == null ? new LinkedHashSet<>() : new LinkedHashSet<>(dangerSigns);
        this.careOnArrival = careOnArrival;
        this.handoverNotes = handoverNotes;
        this.recordedAt = recordedAt;
        this.recordedByUserId = actor.userId();
        this.recordedByUsername = actor.username();
    }

    public UUID getId() { return id; }
    public UUID getPassageId() { return passageId; }
    public String getChiefComplaint() { return chiefComplaint; }
    public boolean isInjury() { return injury; }
    public TriagePriority getPriority() { return priority; }
    public String getPriorityReason() { return priorityReason; }
    public Integer getHeartRateBpm() { return heartRateBpm; }
    public Integer getRespiratoryRatePerMinute() { return respiratoryRatePerMinute; }
    public Integer getSystolicBloodPressure() { return systolicBloodPressure; }
    public Integer getDiastolicBloodPressure() { return diastolicBloodPressure; }
    public BigDecimal getTemperatureCelsius() { return temperatureCelsius; }
    public Integer getOxygenSaturationPercent() { return oxygenSaturationPercent; }
    public Integer getRandomBloodGlucoseMgDl() { return randomBloodGlucoseMgDl; }
    public Integer getPainScore() { return painScore; }
    public BigDecimal getWeightKg() { return weightKg; }
    public TriageConsciousnessLevel getConsciousnessLevel() { return consciousnessLevel; }
    public TriagePregnancyStatus getPregnancyStatus() { return pregnancyStatus; }
    public Integer getCapillaryRefillSeconds() { return capillaryRefillSeconds; }
    public String getOxygenSupport() { return oxygenSupport; }
    public BigDecimal getOxygenFlowLitersPerMinute() { return oxygenFlowLitersPerMinute; }
    public Set<TriageDangerSign> getDangerSigns() { return Set.copyOf(dangerSigns); }
    public String getCareOnArrival() { return careOnArrival; }
    public String getHandoverNotes() { return handoverNotes; }
    public Instant getRecordedAt() { return recordedAt; }
    public String getRecordedByUsername() { return recordedByUsername; }
}
