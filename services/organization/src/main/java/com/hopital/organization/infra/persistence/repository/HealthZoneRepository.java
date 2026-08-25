package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.HealthZoneEntity;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthZoneRepository extends JpaRepository<HealthZoneEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<HealthZoneEntity> findByCodeIgnoreCase(String code);

    List<HealthZoneEntity> findAllByOrderByNameAsc();
}
