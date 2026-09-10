package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.application.domain.PatientTransferStatus;
import com.hopital.patient.infra.persistence.entity.PatientTransferEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientTransferRepository extends JpaRepository<PatientTransferEntity, UUID>,
        JpaSpecificationExecutor<PatientTransferEntity> {
    boolean existsBySourcePassage_IdAndStatusNot(UUID passageId, PatientTransferStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT transfer FROM PatientTransferEntity transfer WHERE transfer.id = :id")
    Optional<PatientTransferEntity> findForUpdate(@Param("id") UUID id);
}
