package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisResultValueEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisResultValueRepository extends JpaRepository<AnalysisResultValueEntity, UUID> {
    List<AnalysisResultValueEntity> findAllByAnalysisResult_IdOrderByRequestParameter_DisplayOrderAsc(UUID resultId);
    List<AnalysisResultValueEntity> findAllByAnalysisResult_IdInOrderByRequestParameter_DisplayOrderAsc(Collection<UUID> resultIds);
}
