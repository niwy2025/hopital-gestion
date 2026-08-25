package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.ProvinceEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProvinceRepository extends JpaRepository<ProvinceEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<ProvinceEntity> findByCodeIgnoreCase(String code);

    List<ProvinceEntity> findAllByOrderByNameAsc();

    @Query("""
            SELECT province
            FROM ProvinceEntity province
            WHERE :query IS NULL
               OR LOWER(province.code) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(province.name) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<ProvinceEntity> search(@Param("query") String query, Pageable pageable);
}
