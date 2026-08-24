package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.HospitalEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<HospitalEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    List<HospitalEntity> findAllByOrderByNameAsc();
}
