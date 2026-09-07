package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.application.domain.TriagePriority;
import com.hopital.patient.infra.persistence.entity.PatientPassageTriageAssessmentEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientPassageTriageAssessmentRepository
        extends JpaRepository<PatientPassageTriageAssessmentEntity, UUID> {

    @Query("""
            SELECT assessment
            FROM PatientPassageTriageAssessmentEntity assessment
            WHERE assessment.passageId = :passageId
              AND (:query = ''
                    OR LOWER(assessment.chiefComplaint) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(assessment.priorityReason) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(assessment.careOnArrival, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(assessment.handoverNotes, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(assessment.recordedByUsername) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:priority IS NULL OR assessment.priority = :priority)
            """)
    Page<PatientPassageTriageAssessmentEntity> search(
            @Param("passageId") UUID passageId,
            @Param("query") String query,
            @Param("priority") TriagePriority priority,
            Pageable pageable);
}
