package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequestEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<AnalysisRequestEntity> findByCodeIgnoreCase(String code);

    List<AnalysisRequestEntity> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT analysisRequest
            FROM AnalysisRequestEntity analysisRequest
            WHERE (:query = ''
                    OR LOWER(analysisRequest.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.patientReference) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.patientName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.analysisCode) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.analysisName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.laboratoryCode) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<AnalysisRequestEntity> search(@Param("query") String query, Pageable pageable);
}
