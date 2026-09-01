package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.PrescriptionSource;
import com.hopital.patient.application.domain.PrescriptionStatus;
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
@Table(name = "patient_passage_prescriptions")
public class PatientPassagePrescriptionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passage_id", nullable = false)
    private PatientPassageEntity passage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrescriptionSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrescriptionStatus status;

    @Column(name = "external_prescriber_name", length = 200)
    private String externalPrescriberName;

    @Column(name = "external_reference", length = 150)
    private String externalReference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by_user_id", nullable = false, length = 100)
    private String createdByUserId;

    @Column(name = "created_by_username", nullable = false, length = 150)
    private String createdByUsername;

    protected PatientPassagePrescriptionEntity() {
    }

    public PatientPassagePrescriptionEntity(
            UUID id,
            String code,
            PatientPassageEntity passage,
            PrescriptionSource source,
            String externalPrescriberName,
            String externalReference,
            String notes,
            AuditActor actor,
            Instant createdAt) {
        this.id = id;
        this.code = code;
        this.passage = passage;
        this.source = source;
        this.status = PrescriptionStatus.PENDING_DISPENSING;
        this.externalPrescriberName = externalPrescriberName;
        this.externalReference = externalReference;
        this.notes = notes;
        this.createdAt = createdAt;
        this.createdByUserId = actor.userId();
        this.createdByUsername = actor.username();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public PatientPassageEntity getPassage() { return passage; }
    public PrescriptionSource getSource() { return source; }
    public PrescriptionStatus getStatus() { return status; }
    public String getExternalPrescriberName() { return externalPrescriberName; }
    public String getExternalReference() { return externalReference; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedByUsername() { return createdByUsername; }
}
