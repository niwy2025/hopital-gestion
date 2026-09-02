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
@Table(name = "patient_passage_prescription_dispense_items")
public class PatientPassagePrescriptionDispenseItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dispense_id", nullable = false)
    private PatientPassagePrescriptionDispenseEntity dispense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_item_id", nullable = false)
    private PatientPassagePrescriptionItemEntity prescriptionItem;

    @Column(name = "dispensed_quantity", nullable = false, length = 100)
    private String dispensedQuantity;

    protected PatientPassagePrescriptionDispenseItemEntity() {
    }

    public PatientPassagePrescriptionDispenseItemEntity(
            UUID id,
            PatientPassagePrescriptionDispenseEntity dispense,
            PatientPassagePrescriptionItemEntity prescriptionItem,
            String dispensedQuantity) {
        this.id = id;
        this.dispense = dispense;
        this.prescriptionItem = prescriptionItem;
        this.dispensedQuantity = dispensedQuantity;
    }

    public UUID getId() { return id; }
    public PatientPassagePrescriptionDispenseEntity getDispense() { return dispense; }
    public PatientPassagePrescriptionItemEntity getPrescriptionItem() { return prescriptionItem; }
    public String getDispensedQuantity() { return dispensedQuantity; }
}
