package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionItemEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientPassagePrescriptionItemRepository extends JpaRepository<PatientPassagePrescriptionItemEntity, UUID> {

    List<PatientPassagePrescriptionItemEntity> findAllByPrescription_IdInOrderByDisplayOrderAsc(
            Collection<UUID> prescriptionIds);
}
