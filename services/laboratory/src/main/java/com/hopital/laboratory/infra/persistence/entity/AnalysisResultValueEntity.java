package com.hopital.laboratory.infra.persistence.entity;

import com.hopital.laboratory.application.domain.AnalysisResultFlag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "analysis_result_values")
public class AnalysisResultValueEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private AnalysisResultEntity analysisResult;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_parameter_id", nullable = false)
    private AnalysisRequestParameterEntity requestParameter;

    @Column(name = "numeric_value", precision = 19, scale = 6)
    private BigDecimal numericValue;

    @Column(name = "integer_value")
    private Long integerValue;

    @Column(name = "text_value", length = 2000)
    private String textValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "qualitative_value", length = 255)
    private String qualitativeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "abnormal_flag", nullable = false, length = 20)
    private AnalysisResultFlag abnormalFlag;

    @Column(length = 1000)
    private String comment;

    protected AnalysisResultValueEntity() {
    }

    public AnalysisResultValueEntity(
            UUID id,
            AnalysisResultEntity analysisResult,
            AnalysisRequestParameterEntity requestParameter,
            BigDecimal numericValue,
            Long integerValue,
            String textValue,
            Boolean booleanValue,
            String qualitativeValue,
            AnalysisResultFlag abnormalFlag,
            String comment) {
        this.id = id;
        this.analysisResult = analysisResult;
        this.requestParameter = requestParameter;
        this.numericValue = numericValue;
        this.integerValue = integerValue;
        this.textValue = textValue;
        this.booleanValue = booleanValue;
        this.qualitativeValue = qualitativeValue;
        this.abnormalFlag = abnormalFlag;
        this.comment = comment;
    }

    public UUID getId() { return id; }
    public AnalysisResultEntity getAnalysisResult() { return analysisResult; }
    public AnalysisRequestParameterEntity getRequestParameter() { return requestParameter; }
    public BigDecimal getNumericValue() { return numericValue; }
    public Long getIntegerValue() { return integerValue; }
    public String getTextValue() { return textValue; }
    public Boolean getBooleanValue() { return booleanValue; }
    public String getQualitativeValue() { return qualitativeValue; }
    public AnalysisResultFlag getAbnormalFlag() { return abnormalFlag; }
    public String getComment() { return comment; }
}
