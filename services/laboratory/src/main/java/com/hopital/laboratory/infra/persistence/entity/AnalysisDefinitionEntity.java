package com.hopital.laboratory.infra.persistence.entity;

import com.hopital.laboratory.application.domain.SpecimenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_definitions")
public class AnalysisDefinitionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "specimen_type", nullable = false, length = 30)
    private SpecimenType specimenType;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    protected AnalysisDefinitionEntity() {
    }

    public AnalysisDefinitionEntity(
            UUID id,
            String code,
            String name,
            String description,
            SpecimenType specimenType,
            Instant createdAt,
            String createdBy) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.specimenType = specimenType;
        this.active = true;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public SpecimenType getSpecimenType() { return specimenType; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
}
