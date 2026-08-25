package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.HealthAreaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HealthAreaRepository extends JpaRepository<HealthAreaEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<HealthAreaEntity> findByCodeIgnoreCase(String code);

    List<HealthAreaEntity> findAllByOrderByNameAsc();

    @Query("""
            SELECT healthArea
            FROM HealthAreaEntity healthArea
            JOIN healthArea.healthZone healthZone
            JOIN healthZone.province province
            WHERE (:query IS NULL
                    OR LOWER(healthArea.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(healthArea.name) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:provinceCode IS NULL OR LOWER(province.code) = LOWER(:provinceCode))
            """)
    Page<HealthAreaEntity> search(
            @Param("query") String query,
            @Param("provinceCode") String provinceCode,
            Pageable pageable);
}
