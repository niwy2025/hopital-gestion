package com.hopital.personnel.infra.persistence.repository;

import com.hopital.personnel.application.domain.PersonnelDocumentType;
import com.hopital.personnel.infra.persistence.entity.PersonnelDocumentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonnelDocumentRepository extends JpaRepository<PersonnelDocumentEntity, UUID> {

    List<PersonnelDocumentEntity> findByPersonnelIdOrderByCreatedAtDesc(UUID personnelId);

    Optional<PersonnelDocumentEntity> findByIdAndPersonnelId(UUID id, UUID personnelId);

    @Modifying
    @Query("DELETE FROM PersonnelDocumentEntity document WHERE document.personnelId = :personnelId AND document.documentType = :documentType")
    void deleteByPersonnelIdAndDocumentType(
            @Param("personnelId") UUID personnelId,
            @Param("documentType") PersonnelDocumentType documentType);
}
