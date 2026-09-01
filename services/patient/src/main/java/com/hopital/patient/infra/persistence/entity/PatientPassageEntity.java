package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient_passages")
public class PatientPassageEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "hospital_code", nullable = false, length = 30)
    private String hospitalCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PatientPassageType type;

    @Column(name = "service_name", length = 150)
    private String serviceName;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PatientPassageStatus status;

    @Column(name = "arrived_at", nullable = false)
    private Instant arrivedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "created_by_user_id", nullable = false, length = 100)
    private String createdByUserId;

    @Column(name = "created_by_username", nullable = false, length = 150)
    private String createdByUsername;

    @Column(name = "closed_by_user_id", length = 100)
    private String closedByUserId;

    @Column(name = "closed_by_username", length = 150)
    private String closedByUsername;

    @Column(name = "responsible_personnel_id")
    private UUID responsiblePersonnelId;

    @Column(name = "responsible_personnel_employee_number", length = 40)
    private String responsiblePersonnelEmployeeNumber;

    @Column(name = "responsible_personnel_name", length = 250)
    private String responsiblePersonnelName;

    @Column(name = "responsible_personnel_job_title", length = 150)
    private String responsiblePersonnelJobTitle;

    @Column(name = "responsible_assigned_at")
    private Instant responsibleAssignedAt;

    @Column(name = "responsible_assigned_by_user_id", length = 100)
    private String responsibleAssignedByUserId;

    @Column(name = "responsible_assigned_by_username", length = 150)
    private String responsibleAssignedByUsername;

    protected PatientPassageEntity() {
    }

    public PatientPassageEntity(
            UUID id,
            String code,
            PatientEntity patient,
            UUID hospitalId,
            String hospitalCode,
            PatientPassageType type,
            String serviceName,
            String reason,
            AuditActor actor,
            Instant arrivedAt) {
        this.id = id;
        this.code = code;
        this.patient = patient;
        this.hospitalId = hospitalId;
        this.hospitalCode = hospitalCode;
        this.type = type;
        this.serviceName = serviceName;
        this.reason = reason;
        this.status = PatientPassageStatus.OPEN;
        this.arrivedAt = arrivedAt;
        this.createdByUserId = actor.userId();
        this.createdByUsername = actor.username();
    }

    public void changeStatus(PatientPassageStatus status, AuditActor actor, Instant changedAt) {
        this.status = status;
        if (status == PatientPassageStatus.OPEN) {
            this.closedAt = null;
            this.closedByUserId = null;
            this.closedByUsername = null;
            return;
        }
        this.closedAt = changedAt;
        this.closedByUserId = actor.userId();
        this.closedByUsername = actor.username();
    }

    public void assignResponsiblePersonnel(
            UUID personnelId,
            String employeeNumber,
            String fullName,
            String jobTitle,
            AuditActor actor,
            Instant assignedAt) {
        this.responsiblePersonnelId = personnelId;
        this.responsiblePersonnelEmployeeNumber = employeeNumber;
        this.responsiblePersonnelName = fullName;
        this.responsiblePersonnelJobTitle = jobTitle;
        this.responsibleAssignedAt = assignedAt;
        this.responsibleAssignedByUserId = actor.userId();
        this.responsibleAssignedByUsername = actor.username();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public PatientEntity getPatient() { return patient; }
    public UUID getHospitalId() { return hospitalId; }
    public String getHospitalCode() { return hospitalCode; }
    public PatientPassageType getType() { return type; }
    public String getServiceName() { return serviceName; }
    public String getReason() { return reason; }
    public PatientPassageStatus getStatus() { return status; }
    public Instant getArrivedAt() { return arrivedAt; }
    public Instant getClosedAt() { return closedAt; }
    public String getCreatedByUsername() { return createdByUsername; }
    public String getClosedByUsername() { return closedByUsername; }
    public UUID getResponsiblePersonnelId() { return responsiblePersonnelId; }
    public String getResponsiblePersonnelEmployeeNumber() { return responsiblePersonnelEmployeeNumber; }
    public String getResponsiblePersonnelName() { return responsiblePersonnelName; }
    public String getResponsiblePersonnelJobTitle() { return responsiblePersonnelJobTitle; }
    public Instant getResponsibleAssignedAt() { return responsibleAssignedAt; }
    public String getResponsibleAssignedByUsername() { return responsibleAssignedByUsername; }
}
