package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionDispenseEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientPassagePrescriptionDispenseRepository
        extends JpaRepository<PatientPassagePrescriptionDispenseEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<PatientPassagePrescriptionDispenseEntity> findByCodeIgnoreCase(String code);

    /** Serializes concurrent accounting events for one immutable dispense. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select dispense from PatientPassagePrescriptionDispenseEntity dispense
            where lower(dispense.code) = lower(:code)
            """)
    Optional<PatientPassagePrescriptionDispenseEntity> lockByCodeIgnoreCase(@Param("code") String code);

    List<PatientPassagePrescriptionDispenseEntity> findAllByPrescription_IdInOrderByDispensedAtDesc(
            Collection<UUID> prescriptionIds);
}
