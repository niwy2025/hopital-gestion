package com.hopital.pharmacy.infra.persistence.repository;

import com.hopital.pharmacy.application.domain.AccountingPostingStatus;
import com.hopital.pharmacy.infra.persistence.entity.StockEntryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockEntryRepository extends JpaRepository<StockEntryEntity, UUID> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<StockEntryEntity> findByCodeIgnoreCase(String code);

    @Query("""
            SELECT entry FROM StockEntryEntity entry
            WHERE (:hospitalCode = '' OR LOWER(entry.hospitalCode) = LOWER(:hospitalCode))
              AND (:query = '' OR LOWER(entry.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(entry.medicine.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(entry.medicine.genericName) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(entry.supplierName, '')) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:accountingStatus IS NULL OR entry.accountingStatus = :accountingStatus)
            """)
    Page<StockEntryEntity> search(
            @Param("hospitalCode") String hospitalCode,
            @Param("query") String query,
            @Param("accountingStatus") AccountingPostingStatus accountingStatus,
            Pageable pageable);
}
