package com.hopital.pharmacy.infra.persistence.repository;

import com.hopital.pharmacy.application.domain.StockMovementAccountingOutboxStatus;
import com.hopital.pharmacy.infra.persistence.entity.StockMovementAccountingOutboxEventEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementAccountingOutboxEventRepository
        extends JpaRepository<StockMovementAccountingOutboxEventEntity, UUID> {

    Optional<StockMovementAccountingOutboxEventEntity> findByStockMovementId(UUID stockMovementId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT event
            FROM StockMovementAccountingOutboxEventEntity event
            WHERE event.stockMovementCode = :stockMovementCode
            """)
    Optional<StockMovementAccountingOutboxEventEntity> lockByStockMovementCode(
            @Param("stockMovementCode") String stockMovementCode);

    List<StockMovementAccountingOutboxEventEntity>
            findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                    StockMovementAccountingOutboxStatus status,
                    Instant nextAttemptAt);
}
