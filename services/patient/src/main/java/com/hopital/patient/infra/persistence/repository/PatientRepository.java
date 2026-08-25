package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<PatientEntity> findByCodeIgnoreCase(String code);

    List<PatientEntity> findAllByOrderByLastNameAscFirstNameAsc();
}
