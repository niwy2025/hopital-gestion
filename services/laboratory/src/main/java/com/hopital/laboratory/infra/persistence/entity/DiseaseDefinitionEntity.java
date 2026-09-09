package com.hopital.laboratory.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "disease_definitions")
public class DiseaseDefinitionEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 30) private String code;
    @Column(nullable = false, length = 200) private String name;
    @Column(length = 1000) private String description;
    @Column(name = "icd_system", length = 30) private String icdSystem;
    @Column(name = "icd_code", length = 50) private String icdCode;
    @Column(name = "icd_uri", length = 500) private String icdUri;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "created_by", length = 100) private String createdBy;

    protected DiseaseDefinitionEntity() { }

    public DiseaseDefinitionEntity(UUID id, String code, String name, String description, String icdSystem,
            String icdCode, String icdUri, Instant createdAt, String createdBy) {
        this.id = id; this.code = code; this.name = name; this.description = description;
        this.icdSystem = icdSystem; this.icdCode = icdCode; this.icdUri = icdUri;
        this.active = true; this.createdAt = createdAt; this.createdBy = createdBy;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcdSystem() { return icdSystem; }
    public String getIcdCode() { return icdCode; }
    public String getIcdUri() { return icdUri; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
}
