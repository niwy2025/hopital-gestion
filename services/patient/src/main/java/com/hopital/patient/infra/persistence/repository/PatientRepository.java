package com.hopital.patient.infra.persistence.repository;

import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.application.domain.Gender;
import java.time.LocalDate;
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

    boolean existsByNationalIdentifierIgnoreCase(String nationalIdentifier);

    List<PatientEntity> findAllByOrderByLastNameAscFirstNameAsc();

    List<PatientEntity> findAllByRegistrationHospitalCodeIgnoreCaseOrderByLastNameAscFirstNameAsc(String registrationHospitalCode);

    @Query("""
            SELECT CASE WHEN COUNT(patient) > 0 THEN true ELSE false END
            FROM PatientEntity patient
            WHERE LOWER(patient.lastName) = LOWER(:lastName)
              AND LOWER(patient.firstName) = LOWER(:firstName)
              AND LOWER(COALESCE(patient.middleName, '')) = LOWER(:middleName)
              AND patient.dateOfBirth = :dateOfBirth
              AND patient.gender = :gender
            """)
    boolean existsByIdentity(
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("middleName") String middleName,
            @Param("dateOfBirth") LocalDate dateOfBirth,
            @Param("gender") Gender gender);

    @Query("""
            SELECT patient
            FROM PatientEntity patient
              WHERE (:query = ''
                    OR LOWER(patient.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(patient.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(patient.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(patient.middleName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(patient.nationalIdentifier, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(patient.phoneNumber, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(patient.registrationHospitalCode) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:hospitalCode = '' OR LOWER(patient.registrationHospitalCode) = LOWER(:hospitalCode))
              AND (:hospitalId IS NULL OR patient.registrationHospitalId = :hospitalId)
              AND (:active IS NULL OR patient.active = :active)
            """)
    Page<PatientEntity> search(
            @Param("query") String query,
            @Param("hospitalCode") String hospitalCode,
            @Param("hospitalId") UUID hospitalId,
            @Param("active") Boolean active,
            Pageable pageable);
}
