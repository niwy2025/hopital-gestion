package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.application.domain.AccountingEntryStatus;
import com.hopital.accounting.application.domain.AccountingSourceType;
import com.hopital.accounting.infra.persistence.entity.AccountingEntryEntity;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountingEntryRepository extends JpaRepository<AccountingEntryEntity, UUID> {
    Optional<AccountingEntryEntity> findByHospitalIdAndSourceTypeAndSourceCode(UUID hospitalId, AccountingSourceType sourceType, String sourceCode);
    boolean existsByHospitalIdAndCode(UUID hospitalId, String code);
    long countByPeriodIdAndStatus(UUID periodId, AccountingEntryStatus status);
    long countByHospitalIdAndStatus(UUID hospitalId, AccountingEntryStatus status);
    @Query("""
            SELECT entry FROM AccountingEntryEntity entry
            WHERE entry.hospitalId = :hospitalId
              AND (:status IS NULL OR entry.status = :status)
              AND (:periodId IS NULL OR entry.periodId = :periodId)
              AND (:dateFrom IS NULL OR entry.entryDate >= :dateFrom)
              AND (:dateTo IS NULL OR entry.entryDate <= :dateTo)
              AND (LOWER(entry.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(entry.description) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(entry.sourceCode) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<AccountingEntryEntity> search(@Param("hospitalId") UUID hospitalId, @Param("query") String query,
            @Param("status") AccountingEntryStatus status, @Param("periodId") UUID periodId,
            @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo, Pageable pageable);
}
