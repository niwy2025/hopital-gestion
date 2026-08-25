package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.LaboratoryStructureEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaboratoryStructureRepository extends JpaRepository<LaboratoryStructureEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<LaboratoryStructureEntity> findByCodeIgnoreCase(String code);

    List<LaboratoryStructureEntity> findAllByOrderByNameAsc();

    @Query("""
            SELECT laboratoryStructure
            FROM LaboratoryStructureEntity laboratoryStructure
            JOIN laboratoryStructure.referenceLaboratory referenceLaboratory
            WHERE (:query = ''
                    OR LOWER(laboratoryStructure.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(laboratoryStructure.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(referenceLaboratory.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(referenceLaboratory.name) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<LaboratoryStructureEntity> search(@Param("query") String query, Pageable pageable);
}
