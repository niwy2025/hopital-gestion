package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.ReferenceLaboratoryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReferenceLaboratoryRepository extends JpaRepository<ReferenceLaboratoryEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<ReferenceLaboratoryEntity> findByCodeIgnoreCase(String code);

    List<ReferenceLaboratoryEntity> findAllByOrderByNameAsc();

    @Query("""
            SELECT referenceLaboratory
            FROM ReferenceLaboratoryEntity referenceLaboratory
            JOIN referenceLaboratory.province province
            WHERE (:query IS NULL
                    OR LOWER(referenceLaboratory.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(referenceLaboratory.name) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:provinceCode IS NULL OR LOWER(province.code) = LOWER(:provinceCode))
            """)
    Page<ReferenceLaboratoryEntity> search(
            @Param("query") String query,
            @Param("provinceCode") String provinceCode,
            Pageable pageable);
}
