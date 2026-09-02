package com.hopital.pharmacy.infra.persistence.repository;

import com.hopital.pharmacy.infra.persistence.entity.MedicineEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicineRepository extends JpaRepository<MedicineEntity, UUID> {
    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            SELECT medicine FROM MedicineEntity medicine
            WHERE (:active IS NULL OR medicine.active = :active)
              AND (:query = '' OR LOWER(medicine.code) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(medicine.genericName) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(medicine.commercialName, '')) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<MedicineEntity> search(@Param("query") String query, @Param("active") Boolean active, Pageable pageable);
}
