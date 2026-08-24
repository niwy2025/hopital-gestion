package com.hopital.organization.infra.persistence.repository;

import com.hopital.organization.infra.persistence.entity.ProvinceEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvinceRepository extends JpaRepository<ProvinceEntity, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<ProvinceEntity> findByCodeIgnoreCase(String code);

    List<ProvinceEntity> findAllByOrderByNameAsc();
}
