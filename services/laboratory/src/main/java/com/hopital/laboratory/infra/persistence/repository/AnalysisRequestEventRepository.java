package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRequestEventRepository extends JpaRepository<AnalysisRequestEventEntity, UUID> {

    List<AnalysisRequestEventEntity> findAllByAnalysisRequest_IdOrderByOccurredAtAsc(UUID analysisRequestId);
}
