package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.ReferenceLaboratoryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferenceLaboratoryRepository extends JpaRepository<ReferenceLaboratoryEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<ReferenceLaboratoryEntity> findByCodeIgnoreCase(String code);

    List<ReferenceLaboratoryEntity> findAllByOrderByNameAsc();
}
