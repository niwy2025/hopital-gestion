package com.hopital.accounting.infra.persistence.repository;

import com.hopital.accounting.application.domain.FinancialStatementNoteStatus;
import com.hopital.accounting.infra.persistence.entity.FinancialStatementNoteEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialStatementNoteRepository extends JpaRepository<FinancialStatementNoteEntity, UUID> {
    boolean existsByHospitalIdAndCode(UUID hospitalId, String code);
    long countByHospitalIdAndStatus(UUID hospitalId, FinancialStatementNoteStatus status);
    @Query("""
            SELECT note FROM FinancialStatementNoteEntity note
            WHERE note.hospitalId = :hospitalId
              AND (:periodId IS NULL OR note.periodId = :periodId)
              AND (:status IS NULL OR note.status = :status)
              AND (LOWER(note.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(note.title) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(note.content) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<FinancialStatementNoteEntity> search(@Param("hospitalId") UUID hospitalId, @Param("periodId") UUID periodId,
            @Param("query") String query, @Param("status") FinancialStatementNoteStatus status, Pageable pageable);
}
