package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
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

@Entity
@Table(name = "patient_passage_clinical_records")
public class PatientPassageClinicalRecordEntity {

    @Id
    private UUID id;

    @Column(name = "passage_id", nullable = false, unique = true)
    private UUID passageId;

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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by_user_id", nullable = false, length = 100)
    private String createdByUserId;

    @Column(name = "created_by_username", nullable = false, length = 150)
    private String createdByUsername;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by_user_id", nullable = false, length = 100)
    private String updatedByUserId;

    @Column(name = "updated_by_username", nullable = false, length = 150)
    private String updatedByUsername;

    protected PatientPassageClinicalRecordEntity() {
    }

    public PatientPassageClinicalRecordEntity(
            UUID id,
            UUID passageId,
            String clinicalFindings,
            String diagnosis,
            String carePlan,
            ClinicalOrientation orientation,
            LocalDate followUpOn,
            AuditActor actor,
            Instant recordedAt) {
        this.id = id;
        this.passageId = passageId;
        this.clinicalFindings = clinicalFindings;
        this.diagnosis = diagnosis;
        this.carePlan = carePlan;
        this.orientation = orientation;
        this.followUpOn = followUpOn;
        this.createdAt = recordedAt;
        this.createdByUserId = actor.userId();
        this.createdByUsername = actor.username();
        this.updatedAt = recordedAt;
        this.updatedByUserId = actor.userId();
        this.updatedByUsername = actor.username();
    }

    public void update(
            String clinicalFindings,
            String diagnosis,
            String carePlan,
            ClinicalOrientation orientation,
            LocalDate followUpOn,
            AuditActor actor,
            Instant updatedAt) {
        this.clinicalFindings = clinicalFindings;
        this.diagnosis = diagnosis;
        this.carePlan = carePlan;
        this.orientation = orientation;
        this.followUpOn = followUpOn;
        this.updatedAt = updatedAt;
        this.updatedByUserId = actor.userId();
        this.updatedByUsername = actor.username();
    }

    public UUID getId() { return id; }
    public UUID getPassageId() { return passageId; }
    public String getClinicalFindings() { return clinicalFindings; }
    public String getDiagnosis() { return diagnosis; }
    public String getCarePlan() { return carePlan; }
    public ClinicalOrientation getOrientation() { return orientation; }
    public LocalDate getFollowUpOn() { return followUpOn; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedByUsername() { return createdByUsername; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedByUsername() { return updatedByUsername; }
}
