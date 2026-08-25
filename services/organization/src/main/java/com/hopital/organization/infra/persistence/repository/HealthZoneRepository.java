package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.HealthZoneEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HealthZoneRepository extends JpaRepository<HealthZoneEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<HealthZoneEntity> findByCodeIgnoreCase(String code);

    List<HealthZoneEntity> findAllByOrderByNameAsc();

    @Query("""
            SELECT healthZone
            FROM HealthZoneEntity healthZone
            JOIN healthZone.province province
            WHERE (:query IS NULL
                    OR LOWER(healthZone.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(healthZone.name) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:provinceCode IS NULL OR LOWER(province.code) = LOWER(:provinceCode))
            """)
    Page<HealthZoneEntity> search(
            @Param("query") String query,
            @Param("provinceCode") String provinceCode,
            Pageable pageable);
}
