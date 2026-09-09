package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisResultInterpretationEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultInterpretationRepository extends JpaRepository<AnalysisResultInterpretationEntity, UUID> {
    boolean existsByAnalysisResult_Id(UUID resultId);
    Optional<AnalysisResultInterpretationEntity> findByAnalysisResult_Id(UUID resultId);
}
