package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.application.domain.PrescriptionSource;
import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionEntity;
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
}
