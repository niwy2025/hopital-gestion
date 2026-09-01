package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.application.domain.PatientDocumentType;
import com.hopital.patient.infra.persistence.entity.PatientDocumentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientDocumentRepository extends JpaRepository<PatientDocumentEntity, UUID> {

    @Query("""
            SELECT document
            FROM PatientDocumentEntity document
            WHERE document.patientId = :patientId
              AND (:query = ''
                    OR LOWER(document.fileName) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:documentType IS NULL OR document.documentType = :documentType)
            """)
    Page<PatientDocumentEntity> search(
            @Param("patientId") UUID patientId,
            @Param("query") String query,
            @Param("documentType") PatientDocumentType documentType,
            Pageable pageable);

    Optional<PatientDocumentEntity> findByIdAndPatientId(UUID id, UUID patientId);

    @Modifying
    @Query("DELETE FROM PatientDocumentEntity document WHERE document.patientId = :patientId AND document.documentType = :documentType")
    void deleteByPatientIdAndDocumentType(
            @Param("patientId") UUID patientId,
            @Param("documentType") PatientDocumentType documentType);
}
