package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<PatientEntity> findByCodeIgnoreCase(String code);

    List<PatientEntity> findAllByOrderByLastNameAscFirstNameAsc();

    List<PatientEntity> findAllByRegistrationHospitalCodeIgnoreCaseOrderByLastNameAscFirstNameAsc(String registrationHospitalCode);

    @Query("""
            SELECT patient
            FROM PatientEntity patient
              WHERE (:query = ''
                    OR LOWER(patient.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(patient.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(patient.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(patient.registrationHospitalCode) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:hospitalCode = '' OR LOWER(patient.registrationHospitalCode) = LOWER(:hospitalCode))
            """)
    Page<PatientEntity> search(@Param("query") String query, @Param("hospitalCode") String hospitalCode, Pageable pageable);
}
