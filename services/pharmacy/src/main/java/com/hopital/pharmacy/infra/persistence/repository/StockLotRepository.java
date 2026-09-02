package com.hopital.pharmacy.infra.persistence.repository;

import com.hopital.pharmacy.infra.persistence.entity.StockLotEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockLotRepository extends JpaRepository<StockLotEntity, UUID> {
    List<StockLotEntity> findByStock_IdAndRemainingQuantityGreaterThan(UUID stockId, int quantity);

    @Query("""
            SELECT lot FROM StockLotEntity lot
            WHERE lot.stock.id = :stockId AND lot.remainingQuantity > 0
              AND (lot.expiresOn IS NULL OR lot.expiresOn >= :today)
            ORDER BY lot.expiresOn ASC NULLS LAST, lot.receivedAt ASC
            """)
    List<StockLotEntity> findUsableByStock(
            @Param("stockId") UUID stockId,
            @Param("today") LocalDate today);

    @Query("""
            SELECT lot FROM StockLotEntity lot
            WHERE (:hospitalCode = '' OR LOWER(lot.stock.hospitalCode) = LOWER(:hospitalCode))
              AND lot.remainingQuantity > 0 AND lot.expiresOn < :today
            ORDER BY lot.expiresOn ASC, lot.receivedAt ASC
            """)
    List<StockLotEntity> findExpired(
            @Param("hospitalCode") String hospitalCode,
            @Param("today") LocalDate today);
}
