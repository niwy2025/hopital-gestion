package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.application.domain.AccountingOutboxStatus;
import com.hopital.patient.infra.persistence.entity.PharmacyDispenseAccountingOutboxEventEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PharmacyDispenseAccountingOutboxEventRepository
        extends JpaRepository<PharmacyDispenseAccountingOutboxEventEntity, UUID> {

    Optional<PharmacyDispenseAccountingOutboxEventEntity> findByDispenseId(UUID dispenseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT event
            FROM PharmacyDispenseAccountingOutboxEventEntity event
            WHERE event.dispenseCode = :dispenseCode
            """)
    Optional<PharmacyDispenseAccountingOutboxEventEntity> lockByDispenseCode(
            @Param("dispenseCode") String dispenseCode);

    List<PharmacyDispenseAccountingOutboxEventEntity>
            findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                    AccountingOutboxStatus status,
                    Instant nextAttemptAt);
}
