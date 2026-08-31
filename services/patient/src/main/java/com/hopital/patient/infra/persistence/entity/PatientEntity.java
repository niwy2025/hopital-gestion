package com.hopital.patient.infra.persistence.entity;

import com.hopital.patient.application.domain.Gender;
import com.hopital.patient.application.domain.EmergencyContactRelationship;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class PatientEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(name = "national_identifier", nullable = false, length = 100)
    private String nationalIdentifier;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<PatientEmergencyContactEntity> emergencyContacts = new ArrayList<>();

    @Column(name = "registration_hospital_id")
    private UUID registrationHospitalId;

    @Column(name = "registration_hospital_code", nullable = false, length = 30)
    private String registrationHospitalCode;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PatientEntity() {
    }

    public PatientEntity(
            UUID id,
            String code,
            String firstName,
            String lastName,
            String middleName,
            LocalDate dateOfBirth,
            Gender gender,
            String phoneNumber,
            String email,
            String address,
            String nationalIdentifier,
            UUID registrationHospitalId,
            String registrationHospitalCode,
            Instant createdAt) {
        this.id = id;
        this.code = code;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.nationalIdentifier = nationalIdentifier;
        this.registrationHospitalId = registrationHospitalId;
        this.registrationHospitalCode = registrationHospitalCode;
        this.active = true;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getNationalIdentifier() {
        return nationalIdentifier;
    }

    public List<PatientEmergencyContactEntity> getEmergencyContacts() { return emergencyContacts; }

    public void addEmergencyContact(
            String fullName,
            String phoneNumber,
            EmergencyContactRelationship relationship,
            int displayOrder) {
        emergencyContacts.add(new PatientEmergencyContactEntity(
                UUID.randomUUID(), this, fullName, phoneNumber, relationship, displayOrder));
    }

    public void replaceEmergencyContacts(List<EmergencyContactData> contacts) {
        emergencyContacts.clear();
        for (int index = 0; index < contacts.size(); index++) {
            EmergencyContactData contact = contacts.get(index);
            addEmergencyContact(contact.fullName(), contact.phoneNumber(), contact.relationship(), index);
        }
    }

    public void updateProfile(
            String firstName,
            String lastName,
            String middleName,
            LocalDate dateOfBirth,
            Gender gender,
            String phoneNumber,
            String email,
            String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }

    public record EmergencyContactData(
            String fullName,
            String phoneNumber,
            EmergencyContactRelationship relationship) {
    }

    public UUID getRegistrationHospitalId() {
        return registrationHospitalId;
    }

    public String getRegistrationHospitalCode() {
        return registrationHospitalCode;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
