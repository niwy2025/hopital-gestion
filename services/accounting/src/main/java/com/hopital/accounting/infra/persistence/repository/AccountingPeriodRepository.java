package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.application.domain.AccountingPeriodStatus;
import com.hopital.accounting.infra.persistence.entity.AccountingPeriodEntity;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountingPeriodRepository extends JpaRepository<AccountingPeriodEntity, UUID> {
    Optional<AccountingPeriodEntity> findByHospitalIdAndCode(UUID hospitalId, String code);
    List<AccountingPeriodEntity> findAllByHospitalId(UUID hospitalId);
    boolean existsByHospitalIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(
            UUID hospitalId, LocalDate startsOn, LocalDate endsOn);
    @Query("""
            SELECT period FROM AccountingPeriodEntity period
            WHERE period.hospitalId = :hospitalId
              AND (:status IS NULL OR period.status = :status)
              AND (LOWER(period.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(period.label) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<AccountingPeriodEntity> search(@Param("hospitalId") UUID hospitalId, @Param("query") String query,
            @Param("status") AccountingPeriodStatus status, Pageable pageable);
    @Query("""
            SELECT period FROM AccountingPeriodEntity period
            WHERE period.hospitalId = :hospitalId AND period.status = 'OPEN'
              AND period.startsOn <= :entryDate AND period.endsOn >= :entryDate
            """)
    Optional<AccountingPeriodEntity> findOpenContaining(@Param("hospitalId") UUID hospitalId, @Param("entryDate") LocalDate entryDate);
}
