package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.PassageResponsibilityStepKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient_passage_responsibility_steps")
public class PatientPassageResponsibilityStepEntity {

    @Id
    private UUID id;

    @Column(name = "passage_id", nullable = false)
    private UUID passageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_kind", nullable = false, length = 40)
    private PassageResponsibilityStepKind stepKind;

    @Column(name = "step_label", nullable = false, length = 150)
    private String stepLabel;

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "responsible_personnel_id", nullable = false)
    private UUID responsiblePersonnelId;

    @Column(name = "responsible_personnel_employee_number", length = 40)
    private String responsiblePersonnelEmployeeNumber;

    @Column(name = "responsible_personnel_name", nullable = false, length = 250)
    private String responsiblePersonnelName;

    @Column(name = "responsible_personnel_job_title", length = 150)
    private String responsiblePersonnelJobTitle;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "assigned_by_user_id", nullable = false, length = 100)
    private String assignedByUserId;

    @Column(name = "assigned_by_username", nullable = false, length = 150)
    private String assignedByUsername;

    protected PatientPassageResponsibilityStepEntity() {
    }

    public PatientPassageResponsibilityStepEntity(
            UUID id,
            UUID passageId,
            PassageResponsibilityStepKind stepKind,
            String stepLabel,
            UUID relatedEntityId,
            UUID responsiblePersonnelId,
            String responsiblePersonnelEmployeeNumber,
            String responsiblePersonnelName,
            String responsiblePersonnelJobTitle,
            AuditActor actor,
            Instant assignedAt) {
        this.id = id;
        this.passageId = passageId;
        this.stepKind = stepKind;
        this.stepLabel = stepLabel;
        this.relatedEntityId = relatedEntityId;
        this.responsiblePersonnelId = responsiblePersonnelId;
        this.responsiblePersonnelEmployeeNumber = responsiblePersonnelEmployeeNumber;
        this.responsiblePersonnelName = responsiblePersonnelName;
        this.responsiblePersonnelJobTitle = responsiblePersonnelJobTitle;
        this.assignedAt = assignedAt;
        this.assignedByUserId = actor.userId();
        this.assignedByUsername = actor.username();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPassageId() {
        return passageId;
    }

    public PassageResponsibilityStepKind getStepKind() {
        return stepKind;
    }

    public String getStepLabel() {
        return stepLabel;
    }

    public UUID getRelatedEntityId() {
        return relatedEntityId;
    }

    public UUID getResponsiblePersonnelId() {
        return responsiblePersonnelId;
    }

    public String getResponsiblePersonnelEmployeeNumber() {
        return responsiblePersonnelEmployeeNumber;
    }

    public String getResponsiblePersonnelName() {
        return responsiblePersonnelName;
    }

    public String getResponsiblePersonnelJobTitle() {
        return responsiblePersonnelJobTitle;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public String getAssignedByUsername() {
        return assignedByUsername;
    }
}
