package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisResultEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResultEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByAnalysisRequest_Id(UUID analysisRequestId);

    Optional<AnalysisResultEntity> findByCodeIgnoreCase(String code);

    List<AnalysisResultEntity> findAllByOrderByEnteredAtDesc();
}
