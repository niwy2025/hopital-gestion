package com.hopital.pharmacy.infra.persistence.repository;

import com.hopital.pharmacy.application.domain.StockMovementType;
import com.hopital.pharmacy.infra.persistence.entity.StockMovementEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {
    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            SELECT movement FROM StockMovementEntity movement
            WHERE (:hospitalCode = '' OR LOWER(movement.hospitalCode) = LOWER(:hospitalCode))
              AND (:medicineId IS NULL OR movement.medicine.id = :medicineId)
              AND (:type IS NULL OR movement.type = :type)
              AND (:query = '' OR LOWER(movement.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(movement.medicine.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(movement.medicine.genericName) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(movement.notes, '')) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<StockMovementEntity> search(
            @Param("hospitalCode") String hospitalCode,
            @Param("medicineId") UUID medicineId,
            @Param("type") StockMovementType type,
            @Param("query") String query,
            Pageable pageable);
}
