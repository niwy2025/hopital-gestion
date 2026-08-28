package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.HospitalLaboratoryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HospitalLaboratoryRepository extends JpaRepository<HospitalLaboratoryEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<HospitalLaboratoryEntity> findByCodeIgnoreCase(String code);

    List<HospitalLaboratoryEntity> findAllByOrderByNameAsc();

    List<HospitalLaboratoryEntity> findAllByHospital_IdAndActiveTrueOrderByNameAsc(UUID hospitalId);

    @Query("""
            SELECT hospitalLaboratory
            FROM HospitalLaboratoryEntity hospitalLaboratory
            JOIN hospitalLaboratory.hospital hospital
            WHERE (:query = ''
                    OR LOWER(hospitalLaboratory.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(hospitalLaboratory.name) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(hospital.code) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(hospital.name) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<HospitalLaboratoryEntity> search(@Param("query") String query, Pageable pageable);
}
