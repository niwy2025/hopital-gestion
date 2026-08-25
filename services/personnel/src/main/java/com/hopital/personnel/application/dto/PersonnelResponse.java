package com.hopital.personnel.application.dto;

import com.hopital.personnel.application.domain.Gender;
import com.hopital.personnel.application.domain.PersonnelCategory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PersonnelResponse(
        UUID id,
        String employeeNumber,
        String firstName,
        String lastName,
        String middleName,
        LocalDate dateOfBirth,
        Gender gender,
        PersonnelCategory category,
        String jobTitle,
        String phoneNumber,
        String email,
        String address,
        UUID hospitalId,
        UUID accountId,
        boolean active,
        Instant createdAt) {
}
