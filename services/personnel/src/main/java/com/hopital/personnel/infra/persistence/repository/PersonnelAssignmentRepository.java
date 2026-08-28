package com.hopital.personnel.infra.persistence.repository;

import com.hopital.personnel.application.domain.PersonnelAssignmentStatus;
import com.hopital.personnel.infra.persistence.entity.PersonnelAssignmentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonnelAssignmentRepository extends JpaRepository<PersonnelAssignmentEntity, UUID> {

    boolean existsByPersonnelIdAndStatusAndPrimaryAssignmentTrue(UUID personnelId, PersonnelAssignmentStatus status);

    Optional<PersonnelAssignmentEntity> findByIdAndPersonnelId(UUID id, UUID personnelId);

    @Query("""
            SELECT assignment FROM PersonnelAssignmentEntity assignment
            WHERE assignment.personnelId = :personnelId
              AND (:status IS NULL OR assignment.status = :status)
              AND (:query = ''
                   OR LOWER(assignment.positionTitle) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(assignment.departmentName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(COALESCE(assignment.unitName, '')) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<PersonnelAssignmentEntity> search(
            @Param("personnelId") UUID personnelId,
            @Param("query") String query,
            @Param("status") PersonnelAssignmentStatus status,
            Pageable pageable);
}
