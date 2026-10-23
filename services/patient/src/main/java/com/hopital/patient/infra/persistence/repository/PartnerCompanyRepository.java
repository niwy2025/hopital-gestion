package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PartnerCompanyEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartnerCompanyRepository extends JpaRepository<PartnerCompanyEntity, UUID> {

    boolean existsByHospitalIdAndCodeIgnoreCase(UUID hospitalId, String code);

    Optional<PartnerCompanyEntity> findByIdAndHospitalId(UUID id, UUID hospitalId);

    @Query("""
            SELECT company FROM PartnerCompanyEntity company
            WHERE company.hospitalId = :hospitalId
              AND (:active IS NULL OR company.active = :active)
              AND (
                :query = ''
                OR LOWER(company.code) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(company.name) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY company.name ASC
            """)
    Page<PartnerCompanyEntity> search(
            @Param("hospitalId") UUID hospitalId,
            @Param("query") String query,
            @Param("active") Boolean active,
            Pageable pageable);
}
