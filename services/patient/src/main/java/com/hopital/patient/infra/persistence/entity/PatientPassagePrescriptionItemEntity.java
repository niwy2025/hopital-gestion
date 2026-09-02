package com.hopital.patient.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "patient_passage_prescription_items")
public class PatientPassagePrescriptionItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private PatientPassagePrescriptionEntity prescription;

    /** UUID du catalogue pharmacie ; aucune clé étrangère n'est créée entre services. */
    @Column(name = "medicine_id")
    private UUID medicineId;

    @Column(name = "medicine_name", nullable = false, length = 250)
    private String medicineName;

    @Column(length = 150)
    private String dosage;

    @Column(name = "administration_route", length = 100)
    private String administrationRoute;

    @Column(length = 150)
    private String frequency;

    @Column(length = 150)
    private String duration;

    @Column(length = 100)
    private String quantity;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected PatientPassagePrescriptionItemEntity() {
    }

    public PatientPassagePrescriptionItemEntity(
            UUID id,
            PatientPassagePrescriptionEntity prescription,
            UUID medicineId,
            String medicineName,
            String dosage,
            String administrationRoute,
            String frequency,
            String duration,
            String quantity,
            String instructions,
            int displayOrder) {
        this.id = id;
        this.prescription = prescription;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.administrationRoute = administrationRoute;
        this.frequency = frequency;
        this.duration = duration;
        this.quantity = quantity;
        this.instructions = instructions;
        this.displayOrder = displayOrder;
    }

    /** Compatible avec les prescriptions historiques qui n'étaient pas liées au catalogue. */
    public PatientPassagePrescriptionItemEntity(
            UUID id,
            PatientPassagePrescriptionEntity prescription,
            String medicineName,
            String dosage,
            String administrationRoute,
            String frequency,
            String duration,
            String quantity,
            String instructions,
            int displayOrder) {
        this(id, prescription, null, medicineName, dosage, administrationRoute, frequency, duration, quantity, instructions, displayOrder);
    }

    public UUID getId() { return id; }
    public PatientPassagePrescriptionEntity getPrescription() { return prescription; }
    public UUID getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public String getDosage() { return dosage; }
    public String getAdministrationRoute() { return administrationRoute; }
    public String getFrequency() { return frequency; }
    public String getDuration() { return duration; }
    public String getQuantity() { return quantity; }
    public String getInstructions() { return instructions; }
    public int getDisplayOrder() { return displayOrder; }
}
