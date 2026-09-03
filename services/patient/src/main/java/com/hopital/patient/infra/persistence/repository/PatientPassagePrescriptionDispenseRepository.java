package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionDispenseEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientPassagePrescriptionDispenseRepository
        extends JpaRepository<PatientPassagePrescriptionDispenseEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<PatientPassagePrescriptionDispenseEntity> findByCodeIgnoreCase(String code);

    List<PatientPassagePrescriptionDispenseEntity> findAllByPrescription_IdInOrderByDispensedAtDesc(
            Collection<UUID> prescriptionIds);
}
