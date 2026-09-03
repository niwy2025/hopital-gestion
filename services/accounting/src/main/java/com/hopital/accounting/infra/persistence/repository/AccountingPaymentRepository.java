package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.infra.persistence.entity.AccountingPaymentEntity;
import java.time.LocalDate;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingPaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountingPaymentRepository extends JpaRepository<AccountingPaymentEntity, UUID> {
    boolean existsByHospitalIdAndCode(UUID hospitalId, String code);
    @Query("""
            SELECT COALESCE(SUM(payment.amount), 0) FROM AccountingPaymentEntity payment
            WHERE payment.hospitalId = :hospitalId AND payment.currency = :currency AND payment.method = :method
              AND payment.createdAt >= :from AND payment.createdAt <= :to
            """)
    BigDecimal sumReceived(@Param("hospitalId") UUID hospitalId, @Param("currency") AccountingCurrency currency,
            @Param("method") AccountingPaymentMethod method, @Param("from") Instant from, @Param("to") Instant to);
    @Query("""
            SELECT payment FROM AccountingPaymentEntity payment
            WHERE payment.hospitalId = :hospitalId
              AND (:method IS NULL OR payment.method = :method)
              AND (:dateFrom IS NULL OR payment.paidOn >= :dateFrom)
              AND (:dateTo IS NULL OR payment.paidOn <= :dateTo)
              AND (LOWER(payment.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(payment.invoiceCode) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(payment.paymentReference, '')) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<AccountingPaymentEntity> search(@Param("hospitalId") UUID hospitalId, @Param("query") String query,
            @Param("method") AccountingPaymentMethod method,
            @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo, Pageable pageable);
}
