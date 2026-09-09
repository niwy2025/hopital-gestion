package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestParameterEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRequestParameterRepository extends JpaRepository<AnalysisRequestParameterEntity, UUID> {
    List<AnalysisRequestParameterEntity> findAllByAnalysisRequest_IdOrderByDisplayOrderAsc(UUID requestId);
    List<AnalysisRequestParameterEntity> findAllByAnalysisRequest_IdInOrderByDisplayOrderAsc(Collection<UUID> requestIds);
    Optional<AnalysisRequestParameterEntity> findByIdAndAnalysisRequest_Id(UUID id, UUID requestId);
}
