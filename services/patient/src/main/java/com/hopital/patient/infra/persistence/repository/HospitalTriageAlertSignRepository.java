package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.HospitalTriageAlertSignEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HospitalTriageAlertSignRepository extends JpaRepository<HospitalTriageAlertSignEntity, UUID> {

    boolean existsByHospitalIdAndCodeIgnoreCase(UUID hospitalId, String code);

    Optional<HospitalTriageAlertSignEntity> findByIdAndHospitalId(UUID id, UUID hospitalId);

    List<HospitalTriageAlertSignEntity> findAllByHospitalIdAndActiveTrueOrderByDisplayOrderAscLabelAsc(UUID hospitalId);

    List<HospitalTriageAlertSignEntity> findAllByIdInAndHospitalId(Collection<UUID> ids, UUID hospitalId);

    @Query("""
            SELECT alertSign FROM HospitalTriageAlertSignEntity alertSign
            WHERE alertSign.hospitalId = :hospitalId
              AND (:active IS NULL OR alertSign.active = :active)
              AND (
                :query = ''
                OR LOWER(alertSign.code) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
                OR LOWER(alertSign.label) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
              )
            """)
    Page<HospitalTriageAlertSignEntity> search(
            @Param("hospitalId") UUID hospitalId,
            @Param("query") String query,
            @Param("active") Boolean active,
            Pageable pageable);
}
