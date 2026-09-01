package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestEntity;
import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
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

    Optional<AnalysisRequestEntity> findByCodeIgnoreCaseAndPatientPassageId(String code, UUID patientPassageId);

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
              AND (:provinceWide = true OR analysisRequest.laboratoryCode IN :laboratoryCodes)
            """)
    Page<AnalysisRequestEntity> search(@Param("query") String query, @Param("provinceWide") boolean provinceWide,
            @Param("laboratoryCodes") List<String> laboratoryCodes, Pageable pageable);

    default Page<AnalysisRequestEntity> search(String query, Pageable pageable) {
        return search(query, true, List.of("_"), pageable);
    }

    @Query("""
            SELECT analysisRequest
            FROM AnalysisRequestEntity analysisRequest
            WHERE analysisRequest.patientPassageId = :passageId
              AND (:query = ''
                    OR LOWER(analysisRequest.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.analysisCode) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.analysisName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(analysisRequest.requesterName, '')) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:status IS NULL OR analysisRequest.status = :status)
            """)
    Page<AnalysisRequestEntity> searchByPatientPassageId(
            @Param("passageId") UUID passageId,
            @Param("query") String query,
            @Param("status") AnalysisRequestStatus status,
            Pageable pageable);
}
