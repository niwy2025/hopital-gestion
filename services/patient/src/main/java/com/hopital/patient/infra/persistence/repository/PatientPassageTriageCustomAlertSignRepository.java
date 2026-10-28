package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientPassageTriageCustomAlertSignEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientPassageTriageCustomAlertSignRepository
        extends JpaRepository<PatientPassageTriageCustomAlertSignEntity, PatientPassageTriageCustomAlertSignEntity.Pk> {

    List<PatientPassageTriageCustomAlertSignEntity> findAllByTriageAssessmentIdIn(Collection<UUID> assessmentIds);
}
