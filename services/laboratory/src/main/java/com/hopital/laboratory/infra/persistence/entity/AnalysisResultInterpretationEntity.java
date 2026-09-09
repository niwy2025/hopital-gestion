package com.hopital.laboratory.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "analysis_result_interpretations")
public class AnalysisResultInterpretationEntity {
    @Id private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_result_id", nullable = false, unique = true)
    private AnalysisResultEntity analysisResult;

    @Column(name = "clinical_conclusion", nullable = false, length = 2000)
    private String clinicalConclusion;

    @Column(name = "doctor_username", nullable = false, length = 100)
    private String doctorUsername;

    @Column(name = "interpreted_at", nullable = false)
    private Instant interpretedAt;

    @ManyToMany
    @JoinTable(name = "analysis_result_interpretation_diseases",
            joinColumns = @JoinColumn(name = "interpretation_id"),
            inverseJoinColumns = @JoinColumn(name = "disease_id"))
    private Set<DiseaseDefinitionEntity> diseases = new LinkedHashSet<>();

    protected AnalysisResultInterpretationEntity() { }

    public AnalysisResultInterpretationEntity(UUID id, AnalysisResultEntity analysisResult, String clinicalConclusion,
            String doctorUsername, Instant interpretedAt, Set<DiseaseDefinitionEntity> diseases) {
        this.id = id; this.analysisResult = analysisResult; this.clinicalConclusion = clinicalConclusion;
        this.doctorUsername = doctorUsername; this.interpretedAt = interpretedAt;
        this.diseases = new LinkedHashSet<>(diseases);
    }

    public UUID getId() { return id; }
    public AnalysisResultEntity getAnalysisResult() { return analysisResult; }
    public String getClinicalConclusion() { return clinicalConclusion; }
    public String getDoctorUsername() { return doctorUsername; }
    public Instant getInterpretedAt() { return interpretedAt; }
    public Set<DiseaseDefinitionEntity> getDiseases() { return diseases; }
}
