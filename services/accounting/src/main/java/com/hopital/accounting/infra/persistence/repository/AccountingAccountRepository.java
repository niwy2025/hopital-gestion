package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.infra.persistence.entity.AccountingAccountEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountingAccountRepository extends JpaRepository<AccountingAccountEntity, UUID> {
    Optional<AccountingAccountEntity> findByHospitalIdAndAccountNumber(UUID hospitalId, String accountNumber);
    boolean existsByHospitalIdAndAccountNumber(UUID hospitalId, String accountNumber);
    List<AccountingAccountEntity> findAllByHospitalIdOrderByAccountNumberAsc(UUID hospitalId);
    @Query("""
            SELECT account FROM AccountingAccountEntity account
            WHERE account.hospitalId = :hospitalId
              AND (:active IS NULL OR account.active = :active)
              AND (LOWER(account.accountNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(account.label) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<AccountingAccountEntity> search(@Param("hospitalId") UUID hospitalId, @Param("query") String query,
            @Param("active") Boolean active, Pageable pageable);
}
