package com.hopital.personnel.application.dto;

import com.hopital.personnel.application.domain.Gender;
import com.hopital.personnel.application.domain.PersonnelCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdatePersonnelRequest(
        @NotBlank @Size(max = 40) String employeeNumber,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 100) String middleName,
        LocalDate dateOfBirth,
        @NotNull Gender gender,
        @NotNull PersonnelCategory category,
        @NotBlank @Size(max = 150) String jobTitle,
        @Size(max = 30) String phoneNumber,
        @Email @Size(max = 255) String email,
        @Size(max = 255) String address,
        String hospitalId,
        String accountId) {
}
