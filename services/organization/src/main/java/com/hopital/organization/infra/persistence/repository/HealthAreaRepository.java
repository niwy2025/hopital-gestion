package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.HealthAreaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthAreaRepository extends JpaRepository<HealthAreaEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<HealthAreaEntity> findByCodeIgnoreCase(String code);

    List<HealthAreaEntity> findAllByOrderByNameAsc();
}
