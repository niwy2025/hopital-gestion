package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestEntity;
import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.LaboratoryType;
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
              AND (:provinceWide = true
                    OR analysisRequest.laboratoryCode IN :laboratoryCodes
                    OR (:originHospitalId IS NOT NULL AND analysisRequest.originHospitalId = :originHospitalId))
            """)
    Page<AnalysisRequestEntity> search(@Param("query") String query, @Param("provinceWide") boolean provinceWide,
            @Param("laboratoryCodes") List<String> laboratoryCodes, @Param("originHospitalId") UUID originHospitalId,
            Pageable pageable);

    default Page<AnalysisRequestEntity> search(String query, Pageable pageable) {
        return search(query, true, List.of("_"), null, pageable);
    }

    /**
     * Reception queue for provincial reference laboratories. The originating
     * hospital deliberately does not grant visibility here: only the
     * destination laboratory (or a provincial supervisor) can receive or
     * reject the physical specimen.
     */
    @Query("""
            SELECT analysisRequest
            FROM AnalysisRequestEntity analysisRequest
            WHERE analysisRequest.laboratoryType = :laboratoryType
              AND analysisRequest.status = :status
              AND (:query = ''
                    OR LOWER(analysisRequest.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.patientReference) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.patientName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.analysisCode) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.analysisName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(analysisRequest.laboratoryCode) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:provinceWide = true
                    OR analysisRequest.laboratoryCode IN :laboratoryCodes)
            """)
    Page<AnalysisRequestEntity> searchReferenceReceptions(
            @Param("query") String query,
            @Param("laboratoryType") LaboratoryType laboratoryType,
            @Param("status") AnalysisRequestStatus status,
            @Param("provinceWide") boolean provinceWide,
            @Param("laboratoryCodes") List<String> laboratoryCodes,
            Pageable pageable);

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
              AND (:provinceWide = true
                    OR analysisRequest.laboratoryCode IN :laboratoryCodes
                    OR (:originHospitalId IS NOT NULL AND analysisRequest.originHospitalId = :originHospitalId))
            """)
    Page<AnalysisRequestEntity> searchByPatientPassageId(
            @Param("passageId") UUID passageId,
            @Param("query") String query,
            @Param("status") AnalysisRequestStatus status,
            @Param("provinceWide") boolean provinceWide,
            @Param("laboratoryCodes") List<String> laboratoryCodes,
            @Param("originHospitalId") UUID originHospitalId,
            Pageable pageable);
}
