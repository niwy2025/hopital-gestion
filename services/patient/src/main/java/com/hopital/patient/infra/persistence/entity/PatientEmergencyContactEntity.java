package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.EmergencyContactRelationship;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "patient_emergency_contacts")
public class PatientEmergencyContactEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmergencyContactRelationship relationship;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected PatientEmergencyContactEntity() {
    }

    PatientEmergencyContactEntity(
            UUID id,
            PatientEntity patient,
            String fullName,
            String phoneNumber,
            EmergencyContactRelationship relationship,
            int displayOrder) {
        this.id = id;
        this.patient = patient;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
        this.displayOrder = displayOrder;
    }

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public EmergencyContactRelationship getRelationship() { return relationship; }
}
