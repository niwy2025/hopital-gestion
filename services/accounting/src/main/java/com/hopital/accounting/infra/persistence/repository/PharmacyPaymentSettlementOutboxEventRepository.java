package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.application.domain.PharmacyPaymentSettlementOutboxStatus;
import com.hopital.accounting.infra.persistence.entity.PharmacyPaymentSettlementOutboxEventEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PharmacyPaymentSettlementOutboxEventRepository
        extends JpaRepository<PharmacyPaymentSettlementOutboxEventEntity, UUID> {

    boolean existsByEventKey(String eventKey);

    boolean existsByInvoiceIdAndStateVersion(UUID invoiceId, int stateVersion);

    boolean existsByPaymentId(UUID paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT event FROM PharmacyPaymentSettlementOutboxEventEntity event WHERE event.id = :eventId")
    Optional<PharmacyPaymentSettlementOutboxEventEntity> lockById(@Param("eventId") UUID eventId);

    List<PharmacyPaymentSettlementOutboxEventEntity>
            findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                    PharmacyPaymentSettlementOutboxStatus status,
                    Instant nextAttemptAt);
}
