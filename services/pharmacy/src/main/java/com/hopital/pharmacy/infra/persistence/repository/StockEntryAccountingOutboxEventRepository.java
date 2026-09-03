package com.hopital.pharmacy.infra.persistence.repository;

import com.hopital.pharmacy.application.domain.StockEntryAccountingOutboxStatus;
import com.hopital.pharmacy.infra.persistence.entity.StockEntryAccountingOutboxEventEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockEntryAccountingOutboxEventRepository
        extends JpaRepository<StockEntryAccountingOutboxEventEntity, UUID> {

    Optional<StockEntryAccountingOutboxEventEntity> findByStockEntryId(UUID stockEntryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT event
            FROM StockEntryAccountingOutboxEventEntity event
            WHERE event.stockEntryCode = :stockEntryCode
            """)
    Optional<StockEntryAccountingOutboxEventEntity> lockByStockEntryCode(
            @Param("stockEntryCode") String stockEntryCode);

    List<StockEntryAccountingOutboxEventEntity>
            findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                    StockEntryAccountingOutboxStatus status,
                    Instant nextAttemptAt);
}
