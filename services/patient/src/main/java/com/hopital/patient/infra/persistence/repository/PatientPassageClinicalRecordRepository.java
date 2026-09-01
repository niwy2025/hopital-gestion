package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientPassageClinicalRecordEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientPassageClinicalRecordRepository
        extends JpaRepository<PatientPassageClinicalRecordEntity, UUID> {

    Optional<PatientPassageClinicalRecordEntity> findByPassageId(UUID passageId);
}
