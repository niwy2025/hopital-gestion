package com.hopital.personnel.infra.persistence.repository;

import com.hopital.personnel.application.domain.PersonnelAssignmentStatus;
import com.hopital.personnel.infra.persistence.entity.PersonnelEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonnelRepository extends JpaRepository<PersonnelEntity, UUID> {

    boolean existsByEmployeeNumberIgnoreCase(String employeeNumber);

    boolean existsByEmployeeNumberIgnoreCaseAndIdNot(String employeeNumber, UUID id);

    boolean existsByAccountId(UUID accountId);

    boolean existsByAccountIdAndIdNot(UUID accountId, UUID id);

    Optional<PersonnelEntity> findByAccountId(UUID accountId);

    @Query("""
            SELECT personnel
            FROM PersonnelEntity personnel
            WHERE (:query = ''
                    OR LOWER(personnel.employeeNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(personnel.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(personnel.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(personnel.middleName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(personnel.jobTitle) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(personnel.email, '')) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:hospitalId IS NULL OR personnel.hospitalId = :hospitalId)
              AND (:active IS NULL OR personnel.active = :active)
            """)
    Page<PersonnelEntity> search(
            @Param("query") String query,
            @Param("hospitalId") UUID hospitalId,
            @Param("active") Boolean active,
            Pageable pageable);

    @Query("""
            SELECT personnel
            FROM PersonnelEntity personnel
            WHERE personnel.active = TRUE
              AND EXISTS (
                    SELECT assignment.id
                    FROM PersonnelAssignmentEntity assignment
                    WHERE assignment.personnelId = personnel.id
                      AND assignment.hospitalId = :hospitalId
                      AND assignment.status = :assignmentStatus
              )
              AND (:query = ''
                    OR LOWER(personnel.employeeNumber) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(personnel.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(personnel.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(personnel.middleName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(personnel.jobTitle) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<PersonnelEntity> searchActiveCarePersonnel(
            @Param("hospitalId") UUID hospitalId,
            @Param("query") String query,
            @Param("assignmentStatus") PersonnelAssignmentStatus assignmentStatus,
            Pageable pageable);

    Optional<PersonnelEntity> findById(UUID id);
}
