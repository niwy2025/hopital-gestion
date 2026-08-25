package com.hopital.laboratory.infra.persistence.repository;

import com.hopital.laboratory.infra.persistence.entity.SpecimenEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecimenRepository extends JpaRepository<SpecimenEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    List<SpecimenEntity> findAllByOrderByReceivedAtDesc();
}
