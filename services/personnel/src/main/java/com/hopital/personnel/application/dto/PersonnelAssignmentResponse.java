package com.hopital.personnel.application.dto;

import com.hopital.personnel.application.domain.PersonnelAssignmentScope;
import com.hopital.personnel.application.domain.PersonnelAssignmentStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PersonnelAssignmentResponse(
        UUID id,
        UUID personnelId,
        PersonnelAssignmentScope scope,
        UUID hospitalId,
        String laboratoryCode,
        String departmentName,
        String unitName,
        String positionTitle,
        LocalDate startsOn,
        LocalDate endsOn,
        PersonnelAssignmentStatus status,
        boolean primaryAssignment,
        String notes,
        Instant createdAt) {
}
