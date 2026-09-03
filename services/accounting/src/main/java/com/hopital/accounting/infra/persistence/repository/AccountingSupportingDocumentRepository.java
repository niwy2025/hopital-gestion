package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.infra.persistence.entity.AccountingSupportingDocumentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountingSupportingDocumentRepository extends JpaRepository<AccountingSupportingDocumentEntity, UUID> {
    List<AccountingSupportingDocumentEntity> findAllByHospitalIdAndRelatedTypeAndRelatedIdOrderByUploadedAtDesc(UUID hospitalId, String relatedType, UUID relatedId);
}
