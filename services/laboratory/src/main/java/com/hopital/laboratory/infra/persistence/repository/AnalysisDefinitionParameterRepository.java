package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisDefinitionParameterEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisDefinitionParameterRepository extends JpaRepository<AnalysisDefinitionParameterEntity, UUID> {
    List<AnalysisDefinitionParameterEntity> findAllByAnalysisDefinition_IdInOrderByDisplayOrderAsc(Collection<UUID> definitionIds);
    List<AnalysisDefinitionParameterEntity> findAllByAnalysisDefinition_IdOrderByDisplayOrderAsc(UUID definitionId);
}
