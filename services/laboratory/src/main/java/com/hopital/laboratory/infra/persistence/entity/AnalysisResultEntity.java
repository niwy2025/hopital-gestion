package com.hopital.laboratory.infra.persistence.entity;

import com.hopital.laboratory.application.domain.AnalysisResultStatus;
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
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "analysis_results")
public class AnalysisResultEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_request_id", nullable = false, unique = true)
    private AnalysisRequestEntity analysisRequest;

    @Nationalized
    @Column(name = "result_value", nullable = false, length = 1000)
    private String resultValue;

    @Nationalized
    @Column(length = 100)
    private String unit;

    @Nationalized
    @Column(name = "reference_range", length = 255)
    private String referenceRange;

    @Nationalized
    @Column(length = 1000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AnalysisResultStatus status;

    @Column(name = "entered_at", nullable = false)
    private Instant enteredAt;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "validated_by", length = 100)
    private String validatedBy;

    protected AnalysisResultEntity() {
    }

    public AnalysisResultEntity(
            UUID id,
            String code,
            AnalysisRequestEntity analysisRequest,
            String resultValue,
            String unit,
            String referenceRange,
            String comment,
            Instant enteredAt) {
        this.id = id;
        this.code = code;
        this.analysisRequest = analysisRequest;
        this.resultValue = resultValue;
        this.unit = unit;
        this.referenceRange = referenceRange;
        this.comment = comment;
        this.status = AnalysisResultStatus.ENTERED;
        this.enteredAt = enteredAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public AnalysisRequestEntity getAnalysisRequest() {
        return analysisRequest;
    }

    public String getResultValue() {
        return resultValue;
    }

    public String getUnit() {
        return unit;
    }

    public String getReferenceRange() {
        return referenceRange;
    }

    public String getComment() {
        return comment;
    }

    public AnalysisResultStatus getStatus() {
        return status;
    }

    public Instant getEnteredAt() {
        return enteredAt;
    }

    public Instant getValidatedAt() {
        return validatedAt;
    }

    public String getValidatedBy() {
        return validatedBy;
    }

    public void validate(String validatedBy, Instant validatedAt) {
        this.status = AnalysisResultStatus.VALIDATED;
        this.validatedBy = validatedBy;
        this.validatedAt = validatedAt;
    }
}
