package com.hopital.laboratory.infra.persistence.entity;

import com.hopital.laboratory.application.domain.AnalysisValueType;
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
@Table(name = "analysis_definition_parameters")
public class AnalysisDefinitionParameterEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_definition_id", nullable = false)
    private AnalysisDefinitionEntity analysisDefinition;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 30)
    private AnalysisValueType valueType;

    @Column(length = 100)
    private String unit;

    @Column(name = "reference_range", length = 255)
    private String referenceRange;

    @Column(name = "qualitative_options", length = 1000)
    private String qualitativeOptions;

    @Column(nullable = false)
    private boolean required;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected AnalysisDefinitionParameterEntity() {
    }

    public AnalysisDefinitionParameterEntity(
            UUID id,
            AnalysisDefinitionEntity analysisDefinition,
            String code,
            String name,
            AnalysisValueType valueType,
            String unit,
            String referenceRange,
            String qualitativeOptions,
            boolean required,
            int displayOrder) {
        this.id = id;
        this.analysisDefinition = analysisDefinition;
        this.code = code;
        this.name = name;
        this.valueType = valueType;
        this.unit = unit;
        this.referenceRange = referenceRange;
        this.qualitativeOptions = qualitativeOptions;
        this.required = required;
        this.displayOrder = displayOrder;
    }

    public UUID getId() { return id; }
    public AnalysisDefinitionEntity getAnalysisDefinition() { return analysisDefinition; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public AnalysisValueType getValueType() { return valueType; }
    public String getUnit() { return unit; }
    public String getReferenceRange() { return referenceRange; }
    public String getQualitativeOptions() { return qualitativeOptions; }
    public boolean isRequired() { return required; }
    public int getDisplayOrder() { return displayOrder; }
}
