package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.application.domain.ClinicalEntryType;
import com.hopital.patient.application.domain.ClinicalOrientation;
import com.hopital.patient.infra.persistence.entity.PatientPassageClinicalEntryEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientPassageClinicalEntryRepository
        extends JpaRepository<PatientPassageClinicalEntryEntity, UUID> {

    @Query("""
            SELECT entry
            FROM PatientPassageClinicalEntryEntity entry
            WHERE entry.passageId = :passageId
              AND (:query = ''
                    OR LOWER(entry.clinicalFindings) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(entry.diagnosis, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(entry.carePlan, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(entry.recordedByUsername) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:entryType IS NULL OR entry.entryType = :entryType)
              AND (:orientation IS NULL OR entry.orientation = :orientation)
            """)
    Page<PatientPassageClinicalEntryEntity> search(
            @Param("passageId") UUID passageId,
            @Param("query") String query,
            @Param("entryType") ClinicalEntryType entryType,
            @Param("orientation") ClinicalOrientation orientation,
            Pageable pageable);
}
