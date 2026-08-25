package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequestEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<AnalysisRequestEntity> findByCodeIgnoreCase(String code);

    List<AnalysisRequestEntity> findAllByOrderByCreatedAtDesc();
}
