package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.infra.persistence.entity.AccountingEntryLineEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountingEntryLineRepository extends JpaRepository<AccountingEntryLineEntity, UUID> {
    List<AccountingEntryLineEntity> findAllByEntry_IdOrderByLineNumberAsc(UUID entryId);
    @Query("""
            SELECT line FROM AccountingEntryLineEntity line JOIN FETCH line.entry entry
            WHERE entry.hospitalId = :hospitalId AND entry.status IN ('POSTED', 'REVERSED')
              AND (:accountId IS NULL OR line.accountId = :accountId)
              AND (:periodId IS NULL OR entry.periodId = :periodId)
              AND (:dateFrom IS NULL OR entry.entryDate >= :dateFrom)
              AND (:dateTo IS NULL OR entry.entryDate <= :dateTo)
              AND (LOWER(line.accountNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(line.accountLabel) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(entry.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(entry.description) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<AccountingEntryLineEntity> ledger(@Param("hospitalId") UUID hospitalId, @Param("accountId") UUID accountId,
            @Param("periodId") UUID periodId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo,
            @Param("query") String query, Pageable pageable);
    @Query("""
            SELECT line FROM AccountingEntryLineEntity line JOIN FETCH line.entry entry
            WHERE entry.hospitalId = :hospitalId AND entry.status IN ('POSTED', 'REVERSED')
              AND (:periodId IS NULL OR entry.periodId = :periodId)
              AND (:dateFrom IS NULL OR entry.entryDate >= :dateFrom)
              AND (:dateTo IS NULL OR entry.entryDate <= :dateTo)
            ORDER BY line.accountNumber ASC, entry.entryDate ASC, line.lineNumber ASC
            """)
    List<AccountingEntryLineEntity> postedLines(@Param("hospitalId") UUID hospitalId, @Param("periodId") UUID periodId,
            @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
}
