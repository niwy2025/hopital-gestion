package com.hopital.personnel.infra.persistence.entity;

import com.hopital.personnel.application.domain.Gender;
import com.hopital.personnel.application.domain.PersonnelCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "personnel")
public class PersonnelEntity {

    @Id
    private UUID id;

    @Column(name = "employee_number", nullable = false, unique = true, length = 40)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PersonnelCategory category;

    @Column(name = "job_title", nullable = false, length = 150)
    private String jobTitle;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PersonnelEntity() {
    }

    public PersonnelEntity(
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
            Instant createdAt) {
        this.id = id;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.category = category;
        this.jobTitle = jobTitle;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.hospitalId = hospitalId;
        this.accountId = accountId;
        this.active = true;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMiddleName() { return middleName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public Gender getGender() { return gender; }
    public PersonnelCategory getCategory() { return category; }
    public String getJobTitle() { return jobTitle; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public UUID getHospitalId() { return hospitalId; }
    public UUID getAccountId() { return accountId; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }

    public void update(
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
            UUID accountId) {
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.category = category;
        this.jobTitle = jobTitle;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.hospitalId = hospitalId;
        this.accountId = accountId;
    }

    public void updateHospitalAssignment(UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public void setActive(boolean active) { this.active = active; }
}
