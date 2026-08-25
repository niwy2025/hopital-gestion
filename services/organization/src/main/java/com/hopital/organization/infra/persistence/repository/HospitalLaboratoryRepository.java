package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.HospitalLaboratoryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalLaboratoryRepository extends JpaRepository<HospitalLaboratoryEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<HospitalLaboratoryEntity> findByCodeIgnoreCase(String code);

    List<HospitalLaboratoryEntity> findAllByOrderByNameAsc();
}
