package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.LaboratoryStructureEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaboratoryStructureRepository extends JpaRepository<LaboratoryStructureEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<LaboratoryStructureEntity> findByCodeIgnoreCase(String code);

    List<LaboratoryStructureEntity> findAllByOrderByNameAsc();
}
