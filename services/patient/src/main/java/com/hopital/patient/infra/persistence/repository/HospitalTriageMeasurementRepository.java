package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.HospitalTriageMeasurementEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HospitalTriageMeasurementRepository extends JpaRepository<HospitalTriageMeasurementEntity, UUID> {

    boolean existsByHospitalIdAndCodeIgnoreCase(UUID hospitalId, String code);

    Optional<HospitalTriageMeasurementEntity> findByIdAndHospitalId(UUID id, UUID hospitalId);

    List<HospitalTriageMeasurementEntity> findAllByHospitalIdAndActiveTrueOrderByDisplayOrderAscLabelAsc(UUID hospitalId);

    List<HospitalTriageMeasurementEntity> findAllByIdInAndHospitalId(Collection<UUID> ids, UUID hospitalId);

    @Query("""
            SELECT measurement FROM HospitalTriageMeasurementEntity measurement
            WHERE measurement.hospitalId = :hospitalId
              AND (:active IS NULL OR measurement.active = :active)
              AND (
                :query = ''
                OR LOWER(measurement.code) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
                OR LOWER(measurement.label) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
              )
            """)
    Page<HospitalTriageMeasurementEntity> search(
            @Param("hospitalId") UUID hospitalId,
            @Param("query") String query,
            @Param("active") Boolean active,
            Pageable pageable);
}
