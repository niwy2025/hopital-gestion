package com.hopital.personnel.infra.persistence.entity;

import com.hopital.personnel.application.domain.PersonnelAssignmentScope;
import com.hopital.personnel.application.domain.PersonnelAssignmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "personnel_assignments")
public class PersonnelAssignmentEntity {

    @Id
    private UUID id;

    @Column(name = "personnel_id", nullable = false)
    private UUID personnelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PersonnelAssignmentScope scope;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "laboratory_code", length = 30)
    private String laboratoryCode;

    @Nationalized
    @Column(name = "department_name", length = 150)
    private String departmentName;

    @Nationalized
    @Column(name = "unit_name", length = 150)
    private String unitName;

    @Nationalized
    @Column(name = "position_title", nullable = false, length = 150)
    private String positionTitle;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PersonnelAssignmentStatus status;

    @Column(name = "primary_assignment", nullable = false)
    private boolean primaryAssignment;

    @Nationalized
    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PersonnelAssignmentEntity() {
    }

    public PersonnelAssignmentEntity(UUID id, UUID personnelId, PersonnelAssignmentScope scope, UUID hospitalId,
            String laboratoryCode,
            String departmentName, String unitName, String positionTitle, LocalDate startsOn,
            boolean primaryAssignment, String notes, Instant createdAt) {
        this.id = id;
        this.personnelId = personnelId;
        this.scope = scope;
        this.hospitalId = hospitalId;
        this.laboratoryCode = laboratoryCode;
        this.departmentName = departmentName;
        this.unitName = unitName;
        this.positionTitle = positionTitle;
        this.startsOn = startsOn;
        this.status = PersonnelAssignmentStatus.ACTIVE;
        this.primaryAssignment = primaryAssignment;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getPersonnelId() { return personnelId; }
    public PersonnelAssignmentScope getScope() { return scope; }
    public UUID getHospitalId() { return hospitalId; }
    public String getLaboratoryCode() { return laboratoryCode; }
    public String getDepartmentName() { return departmentName; }
    public String getUnitName() { return unitName; }
    public String getPositionTitle() { return positionTitle; }
    public LocalDate getStartsOn() { return startsOn; }
    public LocalDate getEndsOn() { return endsOn; }
    public PersonnelAssignmentStatus getStatus() { return status; }
    public boolean isPrimaryAssignment() { return primaryAssignment; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }

    public void close(LocalDate endsOn) {
        this.endsOn = endsOn;
        this.status = PersonnelAssignmentStatus.ENDED;
        this.primaryAssignment = false;
    }
}
