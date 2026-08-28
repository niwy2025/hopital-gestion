package com.hopital.organization.infra.persistence.entity;

import com.hopital.organization.application.domain.LaboratoryStructureType;
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
@Table(name = "laboratory_structures")
public class LaboratoryStructureEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LaboratoryStructureType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reference_laboratory_id", nullable = false)
    private ReferenceLaboratoryEntity referenceLaboratory;

    @Column(nullable = false)
    private boolean active;

    protected LaboratoryStructureEntity() {
    }

    public LaboratoryStructureEntity(
            UUID id,
            String code,
            String name,
            LaboratoryStructureType type,
            ReferenceLaboratoryEntity referenceLaboratory) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.referenceLaboratory = referenceLaboratory;
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public LaboratoryStructureType getType() {
        return type;
    }

    public ReferenceLaboratoryEntity getReferenceLaboratory() {
        return referenceLaboratory;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
