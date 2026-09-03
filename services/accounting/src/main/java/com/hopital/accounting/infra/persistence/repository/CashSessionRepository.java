package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.CashSessionStatus;
import com.hopital.accounting.infra.persistence.entity.CashSessionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CashSessionRepository extends JpaRepository<CashSessionEntity, UUID> {
    boolean existsByHospitalIdAndCode(UUID hospitalId, String code);
    Optional<CashSessionEntity> findByHospitalIdAndCurrencyAndStatus(UUID hospitalId, AccountingCurrency currency, CashSessionStatus status);
    @Query("""
            SELECT session FROM CashSessionEntity session
            WHERE session.hospitalId = :hospitalId
              AND (:status IS NULL OR session.status = :status)
              AND (LOWER(session.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(session.openedByUsername) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<CashSessionEntity> search(@Param("hospitalId") UUID hospitalId, @Param("query") String query,
            @Param("status") CashSessionStatus status, Pageable pageable);
}
