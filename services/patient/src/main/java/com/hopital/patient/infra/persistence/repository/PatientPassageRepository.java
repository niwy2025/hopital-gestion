package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import com.hopital.patient.infra.persistence.entity.PatientPassageEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientPassageRepository extends JpaRepository<PatientPassageEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            SELECT passage
            FROM PatientPassageEntity passage
            WHERE passage.patient.id = :patientId
              AND (:query = ''
                    OR LOWER(passage.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(passage.serviceName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(passage.reason, '')) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:type IS NULL OR passage.type = :type)
              AND (:status IS NULL OR passage.status = :status)
            """)
    Page<PatientPassageEntity> search(
            @Param("patientId") UUID patientId,
            @Param("query") String query,
            @Param("type") PatientPassageType type,
            @Param("status") PatientPassageStatus status,
            Pageable pageable);

    @Query("""
            SELECT passage
            FROM PatientPassageEntity passage
            JOIN passage.patient patient
            WHERE (:scopeHospitalCode = '' OR passage.hospitalCode = :scopeHospitalCode)
              AND (:hospitalId IS NULL OR passage.hospitalId = :hospitalId)
              AND (:query = ''
                    OR LOWER(passage.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(patient.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(patient.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(patient.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(patient.middleName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(passage.serviceName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(passage.reason, '')) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:type IS NULL OR passage.type = :type)
              AND (:status IS NULL OR passage.status = :status)
            """)
    Page<PatientPassageEntity> searchRegistry(
            @Param("scopeHospitalCode") String scopeHospitalCode,
            @Param("hospitalId") UUID hospitalId,
            @Param("query") String query,
            @Param("type") PatientPassageType type,
            @Param("status") PatientPassageStatus status,
            Pageable pageable);
}
