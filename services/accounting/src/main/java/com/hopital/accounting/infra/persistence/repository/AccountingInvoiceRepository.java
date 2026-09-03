package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.application.domain.AccountingSourceType;
import com.hopital.accounting.application.domain.InvoiceStatus;
import com.hopital.accounting.infra.persistence.entity.AccountingInvoiceEntity;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Collection;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountingInvoiceRepository extends JpaRepository<AccountingInvoiceEntity, UUID> {
    Optional<AccountingInvoiceEntity> findByHospitalIdAndSourceTypeAndSourceCode(UUID hospitalId, AccountingSourceType sourceType, String sourceCode);
    boolean existsByHospitalIdAndCode(UUID hospitalId, String code);
    long countByHospitalIdAndStatusIn(UUID hospitalId, Collection<InvoiceStatus> statuses);
    @Query("SELECT COALESCE(SUM(invoice.dueAmount), 0) FROM AccountingInvoiceEntity invoice WHERE invoice.hospitalId = :hospitalId AND invoice.status IN ('ISSUED', 'PARTIALLY_PAID')")
    BigDecimal sumOutstandingByHospitalId(@Param("hospitalId") UUID hospitalId);
    @Query("""
            SELECT invoice FROM AccountingInvoiceEntity invoice
            WHERE invoice.hospitalId = :hospitalId
              AND (:status IS NULL OR invoice.status = :status)
              AND (:dateFrom IS NULL OR invoice.issuedOn >= :dateFrom)
              AND (:dateTo IS NULL OR invoice.issuedOn <= :dateTo)
              AND (LOWER(invoice.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(invoice.patientCode, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(invoice.passageCode, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(invoice.description) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<AccountingInvoiceEntity> search(@Param("hospitalId") UUID hospitalId, @Param("query") String query,
            @Param("status") InvoiceStatus status, @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo, Pageable pageable);
}
