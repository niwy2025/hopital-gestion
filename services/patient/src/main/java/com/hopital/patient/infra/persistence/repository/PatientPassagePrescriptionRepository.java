package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.application.domain.PrescriptionSource;
import com.hopital.patient.application.domain.PrescriptionStatus;
import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionEntity;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientPassagePrescriptionRepository extends JpaRepository<PatientPassagePrescriptionEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            SELECT prescription
            FROM PatientPassagePrescriptionEntity prescription
            WHERE prescription.passage.id = :passageId
              AND (:query = ''
                    OR LOWER(prescription.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(prescription.externalPrescriberName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(prescription.externalReference, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(prescription.notes, '')) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:source IS NULL OR prescription.source = :source)
            """)
    Page<PatientPassagePrescriptionEntity> search(
            @Param("passageId") UUID passageId,
            @Param("query") String query,
            @Param("source") PrescriptionSource source,
            Pageable pageable);

    @Query("""
            SELECT prescription
            FROM PatientPassagePrescriptionEntity prescription
            WHERE (:hospitalCode = '' OR LOWER(prescription.passage.hospitalCode) = LOWER(:hospitalCode))
              AND (:query = ''
                    OR LOWER(prescription.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(prescription.passage.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(prescription.passage.patient.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(prescription.passage.patient.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(prescription.passage.patient.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(prescription.externalPrescriberName, '')) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:source IS NULL OR prescription.source = :source)
              AND (:status IS NULL OR prescription.status = :status)
            """)
    Page<PatientPassagePrescriptionEntity> searchForPharmacy(
            @Param("hospitalCode") String hospitalCode,
            @Param("query") String query,
            @Param("source") PrescriptionSource source,
            @Param("status") PrescriptionStatus status,
            Pageable pageable);

    boolean existsByPassage_IdAndStatusIn(UUID passageId, Collection<PrescriptionStatus> statuses);
}
