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
@Table(name = "analysis_request_parameters")
public class AnalysisRequestParameterEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_request_id", nullable = false)
    private AnalysisRequestEntity analysisRequest;

    @Column(name = "source_parameter_id")
    private UUID sourceParameterId;

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

    protected AnalysisRequestParameterEntity() {
    }

    public AnalysisRequestParameterEntity(UUID id, AnalysisRequestEntity analysisRequest, AnalysisDefinitionParameterEntity source) {
        this.id = id;
        this.analysisRequest = analysisRequest;
        this.sourceParameterId = source.getId();
        this.code = source.getCode();
        this.name = source.getName();
        this.valueType = source.getValueType();
        this.unit = source.getUnit();
        this.referenceRange = source.getReferenceRange();
        this.qualitativeOptions = source.getQualitativeOptions();
        this.required = source.isRequired();
        this.displayOrder = source.getDisplayOrder();
    }

    public UUID getId() { return id; }
    public AnalysisRequestEntity getAnalysisRequest() { return analysisRequest; }
    public UUID getSourceParameterId() { return sourceParameterId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public AnalysisValueType getValueType() { return valueType; }
    public String getUnit() { return unit; }
    public String getReferenceRange() { return referenceRange; }
    public String getQualitativeOptions() { return qualitativeOptions; }
    public boolean isRequired() { return required; }
    public int getDisplayOrder() { return displayOrder; }
}
