package com.hopital.personnel.application.dto;

import com.hopital.personnel.application.domain.PersonnelCategory;
import java.util.UUID;

/**
 * Minimal, stable identity exposed when another clinical module needs to assign
 * an active agent to a patient's passage.
 */
public record PersonnelCareReferenceResponse(
        UUID id,
        String employeeNumber,
        String firstName,
        String lastName,
        String middleName,
        PersonnelCategory category,
        String jobTitle) {
}
