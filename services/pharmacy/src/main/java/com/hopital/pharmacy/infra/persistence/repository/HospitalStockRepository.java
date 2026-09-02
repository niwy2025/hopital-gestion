package com.hopital.pharmacy.infra.persistence.repository;

import com.hopital.pharmacy.infra.persistence.entity.HospitalStockEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HospitalStockRepository extends JpaRepository<HospitalStockEntity, UUID> {
    Optional<HospitalStockEntity> findByHospitalIdAndMedicine_Id(UUID hospitalId, UUID medicineId);

    @Query("""
            SELECT stock FROM HospitalStockEntity stock
            WHERE (:hospitalCode = '' OR LOWER(stock.hospitalCode) = LOWER(:hospitalCode))
              AND (:query = '' OR LOWER(stock.medicine.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(stock.medicine.genericName) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(stock.medicine.commercialName, '')) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:lowStock = false OR stock.quantity <= stock.reorderLevel)
            """)
    Page<HospitalStockEntity> search(
            @Param("hospitalCode") String hospitalCode,
            @Param("query") String query,
            @Param("lowStock") boolean lowStock,
            Pageable pageable);
}
