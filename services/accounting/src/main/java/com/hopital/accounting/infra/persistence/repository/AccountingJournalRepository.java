package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.infra.persistence.entity.AccountingJournalEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountingJournalRepository extends JpaRepository<AccountingJournalEntity, UUID> {
    Optional<AccountingJournalEntity> findByHospitalIdAndCode(UUID hospitalId, String code);
    boolean existsByHospitalIdAndCode(UUID hospitalId, String code);
    List<AccountingJournalEntity> findAllByHospitalIdOrderByCodeAsc(UUID hospitalId);
    @Query("""
            SELECT journal FROM AccountingJournalEntity journal
            WHERE journal.hospitalId = :hospitalId
              AND (:active IS NULL OR journal.active = :active)
              AND (LOWER(journal.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(journal.label) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<AccountingJournalEntity> search(@Param("hospitalId") UUID hospitalId, @Param("query") String query,
            @Param("active") Boolean active, Pageable pageable);
}
