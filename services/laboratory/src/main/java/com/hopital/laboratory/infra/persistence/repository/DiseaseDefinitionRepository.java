package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.DiseaseDefinitionEntity;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiseaseDefinitionRepository extends JpaRepository<DiseaseDefinitionEntity, UUID> {
    boolean existsByCodeIgnoreCase(String code);
    Set<DiseaseDefinitionEntity> findAllByIdInAndActiveTrue(Set<UUID> ids);

    @Query("""
            SELECT disease FROM DiseaseDefinitionEntity disease
            WHERE (:active IS NULL OR disease.active = :active)
              AND (:query = ''
                    OR LOWER(disease.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(disease.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(disease.icdCode, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(disease.description, '')) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<DiseaseDefinitionEntity> search(@Param("query") String query, @Param("active") Boolean active, Pageable pageable);
}
