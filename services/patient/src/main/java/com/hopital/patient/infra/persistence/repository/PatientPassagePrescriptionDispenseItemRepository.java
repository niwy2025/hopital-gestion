package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionDispenseItemEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientPassagePrescriptionDispenseItemRepository
        extends JpaRepository<PatientPassagePrescriptionDispenseItemEntity, UUID> {

    List<PatientPassagePrescriptionDispenseItemEntity> findAllByDispense_IdInOrderByDispense_IdAsc(
            Collection<UUID> dispenseIds);
}
