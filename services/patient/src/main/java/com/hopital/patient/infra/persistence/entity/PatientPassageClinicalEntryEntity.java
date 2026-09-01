package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.ClinicalEntryType;
import com.hopital.patient.application.domain.ClinicalOrientation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Immutable medical observation recorded within one hospital passage. */
@Entity
@Table(name = "patient_passage_clinical_entries")
public class PatientPassageClinicalEntryEntity {

    @Id
    private UUID id;

    @Column(name = "passage_id", nullable = false)
    private UUID passageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    private ClinicalEntryType entryType;

    @Column(name = "clinical_findings", nullable = false, columnDefinition = "TEXT")
    private String clinicalFindings;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "care_plan", columnDefinition = "TEXT")
    private String carePlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClinicalOrientation orientation;

    @Column(name = "follow_up_on")
    private LocalDate followUpOn;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "recorded_by_user_id", nullable = false, length = 100)
    private String recordedByUserId;

    @Column(name = "recorded_by_username", nullable = false, length = 150)
    private String recordedByUsername;

    protected PatientPassageClinicalEntryEntity() {
    }

    public PatientPassageClinicalEntryEntity(
            UUID id,
            UUID passageId,
            ClinicalEntryType entryType,
            String clinicalFindings,
            String diagnosis,
            String carePlan,
            ClinicalOrientation orientation,
            LocalDate followUpOn,
            AuditActor actor,
            Instant recordedAt) {
        this.id = id;
        this.passageId = passageId;
        this.entryType = entryType;
        this.clinicalFindings = clinicalFindings;
        this.diagnosis = diagnosis;
        this.carePlan = carePlan;
        this.orientation = orientation;
        this.followUpOn = followUpOn;
        this.recordedAt = recordedAt;
        this.recordedByUserId = actor.userId();
        this.recordedByUsername = actor.username();
    }

    public UUID getId() { return id; }
    public UUID getPassageId() { return passageId; }
    public ClinicalEntryType getEntryType() { return entryType; }
    public String getClinicalFindings() { return clinicalFindings; }
    public String getDiagnosis() { return diagnosis; }
    public String getCarePlan() { return carePlan; }
    public ClinicalOrientation getOrientation() { return orientation; }
    public LocalDate getFollowUpOn() { return followUpOn; }
    public Instant getRecordedAt() { return recordedAt; }
    public String getRecordedByUsername() { return recordedByUsername; }
}
