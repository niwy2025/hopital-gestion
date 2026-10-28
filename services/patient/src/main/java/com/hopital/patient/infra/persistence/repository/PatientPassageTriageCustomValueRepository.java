package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientPassageTriageCustomValueEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientPassageTriageCustomValueRepository
        extends JpaRepository<PatientPassageTriageCustomValueEntity, PatientPassageTriageCustomValueEntity.Pk> {

    List<PatientPassageTriageCustomValueEntity> findAllByTriageAssessmentIdIn(Collection<UUID> assessmentIds);
}
