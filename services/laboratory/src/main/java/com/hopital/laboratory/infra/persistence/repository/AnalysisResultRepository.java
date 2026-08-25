package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisResultEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResultEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByAnalysisRequest_Id(UUID analysisRequestId);

    Optional<AnalysisResultEntity> findByCodeIgnoreCase(String code);

    List<AnalysisResultEntity> findAllByOrderByEnteredAtDesc();

    @Query("""
            SELECT analysisResult
            FROM AnalysisResultEntity analysisResult
            JOIN analysisResult.analysisRequest analysisRequest
            WHERE (:query IS NULL
                    OR LOWER(analysisResult.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.patientReference) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.patientName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.analysisCode) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.analysisName) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<AnalysisResultEntity> search(@Param("query") String query, Pageable pageable);
}
