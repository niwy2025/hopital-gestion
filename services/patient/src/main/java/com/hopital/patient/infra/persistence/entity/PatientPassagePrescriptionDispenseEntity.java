package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.PrescriptionDispenseCompletion;
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
@Table(name = "patient_passage_prescription_dispenses")
public class PatientPassagePrescriptionDispenseEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private PatientPassagePrescriptionEntity prescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionDispenseCompletion completion;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "dispensed_at", nullable = false)
    private Instant dispensedAt;

    @Column(name = "dispensed_by_user_id", nullable = false, length = 100)
    private String dispensedByUserId;

    @Column(name = "dispensed_by_username", nullable = false, length = 150)
    private String dispensedByUsername;

    protected PatientPassagePrescriptionDispenseEntity() {
    }

    public PatientPassagePrescriptionDispenseEntity(
            UUID id,
            String code,
            PatientPassagePrescriptionEntity prescription,
            PrescriptionDispenseCompletion completion,
            String notes,
            AuditActor actor,
            Instant dispensedAt) {
        this.id = id;
        this.code = code;
        this.prescription = prescription;
        this.completion = completion;
        this.notes = notes;
        this.dispensedAt = dispensedAt;
        this.dispensedByUserId = actor.userId();
        this.dispensedByUsername = actor.username();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public PatientPassagePrescriptionEntity getPrescription() { return prescription; }
    public PrescriptionDispenseCompletion getCompletion() { return completion; }
    public String getNotes() { return notes; }
    public Instant getDispensedAt() { return dispensedAt; }
    public String getDispensedByUsername() { return dispensedByUsername; }
}
