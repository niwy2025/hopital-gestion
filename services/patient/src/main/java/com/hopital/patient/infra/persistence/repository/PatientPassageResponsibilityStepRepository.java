package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientPassageResponsibilityStepEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientPassageResponsibilityStepRepository
        extends JpaRepository<PatientPassageResponsibilityStepEntity, UUID> {

    @Query("""
            SELECT step FROM PatientPassageResponsibilityStepEntity step
            WHERE step.passageId = :passageId
            ORDER BY step.assignedAt DESC
            """)
    Page<PatientPassageResponsibilityStepEntity> searchByPassageId(
            @Param("passageId") UUID passageId,
            Pageable pageable);
}
