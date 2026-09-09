package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.application.domain.SpecimenType;
import com.hopital.laboratory.infra.persistence.entity.AnalysisDefinitionEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisDefinitionRepository extends JpaRepository<AnalysisDefinitionEntity, UUID> {
    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            SELECT definition FROM AnalysisDefinitionEntity definition
            WHERE (:active IS NULL OR definition.active = :active)
              AND (:specimenType IS NULL OR definition.specimenType = :specimenType)
              AND (:query = ''
                    OR LOWER(definition.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(definition.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(definition.description, '')) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<AnalysisDefinitionEntity> search(@Param("query") String query, @Param("specimenType") SpecimenType specimenType,
            @Param("active") Boolean active, Pageable pageable);
}
