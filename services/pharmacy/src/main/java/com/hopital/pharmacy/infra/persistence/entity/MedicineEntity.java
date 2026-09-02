package com.hopital.pharmacy.infra.persistence.entity;

import com.hopital.pharmacy.application.domain.AuditActor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medicines")
public class MedicineEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 30) private String code;
    @Column(name = "generic_name", nullable = false, length = 200) private String genericName;
    @Column(name = "commercial_name", length = 200) private String commercialName;
    @Column(length = 100) private String dosage;
    @Column(name = "pharmaceutical_form", length = 100) private String pharmaceuticalForm;
    @Column(length = 150) private String presentation;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by_user_id", nullable = false, length = 100) private String createdByUserId;
    @Column(name = "created_by_username", nullable = false, length = 150) private String createdByUsername;

    protected MedicineEntity() { }

    public MedicineEntity(UUID id, String code, String genericName, String commercialName, String dosage,
            String pharmaceuticalForm, String presentation, AuditActor actor, Instant createdAt) {
        this.id = id; this.code = code; this.genericName = genericName; this.commercialName = commercialName;
        this.dosage = dosage; this.pharmaceuticalForm = pharmaceuticalForm; this.presentation = presentation;
        this.active = true; this.createdAt = createdAt; this.createdByUserId = actor.userId(); this.createdByUsername = actor.username();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getGenericName() { return genericName; }
    public String getCommercialName() { return commercialName; }
    public String getDosage() { return dosage; }
    public String getPharmaceuticalForm() { return pharmaceuticalForm; }
    public String getPresentation() { return presentation; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedByUsername() { return createdByUsername; }
}
