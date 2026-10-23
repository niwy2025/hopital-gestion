package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.application.domain.PartnerCompanyDocumentType;
import com.hopital.patient.infra.persistence.entity.PartnerCompanyDocumentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartnerCompanyDocumentRepository extends JpaRepository<PartnerCompanyDocumentEntity, UUID> {

    @Query("""
            SELECT document
            FROM PartnerCompanyDocumentEntity document
            WHERE document.partnerCompanyId = :partnerCompanyId
              AND (:query = ''
                    OR LOWER(document.fileName) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:documentType IS NULL OR document.documentType = :documentType)
            ORDER BY document.createdAt DESC
            """)
    Page<PartnerCompanyDocumentEntity> search(
            @Param("partnerCompanyId") UUID partnerCompanyId,
            @Param("query") String query,
            @Param("documentType") PartnerCompanyDocumentType documentType,
            Pageable pageable);

    Optional<PartnerCompanyDocumentEntity> findByIdAndPartnerCompanyId(UUID id, UUID partnerCompanyId);
}
