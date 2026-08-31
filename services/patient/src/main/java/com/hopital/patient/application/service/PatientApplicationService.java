package com.hopital.patient.application.service;

import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.Gender;
import com.hopital.patient.application.dto.CreatePatientRequest;
import com.hopital.patient.application.dto.EmergencyContactResponse;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.PatientDuplicateCheckRequest;
import com.hopital.patient.application.dto.PatientDuplicateCheckResponse;
import com.hopital.patient.application.dto.PatientResponse;
import com.hopital.patient.application.dto.PatientSummaryResponse;
import com.hopital.patient.application.dto.UpdatePatientStatusRequest;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.exception.DuplicatePatientException;
import com.hopital.patient.application.exception.PatientNotFoundException;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.infra.persistence.repository.PatientRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PatientApplicationService {

    private final PatientRepository patientRepository;
    private final HospitalReferenceClient hospitalReferenceClient;

    public PatientApplicationService(
            PatientRepository patientRepository,
            HospitalReferenceClient hospitalReferenceClient) {
        this.patientRepository = patientRepository;
        this.hospitalReferenceClient = hospitalReferenceClient;
    }

    /** Compatibility endpoint for existing dependent forms. New registry screens use the paginated search endpoint. */
    public List<PatientSummaryResponse> listPatients(DataAccessScope accessScope) {
        List<PatientEntity> patients = accessScope.provinceWide()
                ? patientRepository.findAllByOrderByLastNameAscFirstNameAsc()
                : patientRepository.findAllByRegistrationHospitalCodeIgnoreCaseOrderByLastNameAscFirstNameAsc(
                        accessScope.hospitalCode());
        return patients.stream().map(this::toSummary).toList();
    }

    public PageResponse<PatientSummaryResponse> searchPatients(
            int page,
            int size,
            String query,
            UUID hospitalId,
            Boolean active,
            DataAccessScope accessScope) {
        var patients = patientRepository.search(
                normalizeSearchFilter(query),
                accessScope.provinceWide() ? "" : accessScope.hospitalCode(),
                accessScope.provinceWide() ? hospitalId : null,
                active,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("lastName").ascending().and(Sort.by("firstName").ascending())));
        return new PageResponse<>(
                patients.getContent().stream().map(this::toSummary).toList(),
                patients.getNumber(),
                patients.getSize(),
                patients.getTotalElements(),
                patients.getTotalPages());
    }

    public PatientResponse getPatient(UUID patientId, DataAccessScope accessScope) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        return toDetails(patient);
    }

    public PatientDuplicateCheckResponse checkDuplicates(
            PatientDuplicateCheckRequest request,
            DataAccessScope accessScope) {
        return new PatientDuplicateCheckResponse(findDuplicateCandidates(
                request.firstName(),
                request.lastName(),
                request.middleName(),
                request.dateOfBirth(),
                request.gender()).stream()
                .filter(patient -> accessScope.canAccessHospital(patient.getRegistrationHospitalCode()))
                .map(this::toSummary)
                .toList());
    }

    @Transactional
    public PatientResponse createPatient(CreatePatientRequest request, DataAccessScope accessScope) {
        HospitalReferenceClient.HospitalReference hospital = hospitalReferenceClient.resolveActiveHospital(
                request.registrationHospitalId());
        String registrationHospitalCode = normalizeCode(hospital.hospitalCode());
        assertAccess(accessScope, registrationHospitalCode);

        assertNoDuplicate(request);

        PatientEntity patient = new PatientEntity(
                UUID.randomUUID(),
                nextPatientCode(),
                request.firstName().trim(),
                request.lastName().trim(),
                trimToNull(request.middleName()),
                request.dateOfBirth(),
                request.gender(),
                trimToNull(request.phoneNumber()),
                trimToNull(request.email()),
                trimToNull(request.address()),
                nextNationalIdentifier(),
                hospital.hospitalId(),
                registrationHospitalCode,
                Instant.now());
        for (int index = 0; index < request.emergencyContacts().size(); index++) {
            var contact = request.emergencyContacts().get(index);
            patient.addEmergencyContact(
                    contact.fullName().trim(),
                    contact.phoneNumber().trim(),
                    contact.relationship(),
                    index);
        }
        return toDetails(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse updateStatus(UUID patientId, UpdatePatientStatusRequest request, DataAccessScope accessScope) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        patient.setActive(request.active());
        return toDetails(patient);
    }

    private PatientSummaryResponse toSummary(PatientEntity patient) {
        return new PatientSummaryResponse(
                patient.getId(),
                patient.getCode(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getMiddleName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhoneNumber(),
                patient.getRegistrationHospitalId(),
                patient.getRegistrationHospitalCode(),
                patient.isActive(),
                patient.getCreatedAt());
    }

    private PatientResponse toDetails(PatientEntity patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getCode(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getMiddleName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhoneNumber(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getNationalIdentifier(),
                patient.getEmergencyContacts().stream()
                        .map(contact -> new EmergencyContactResponse(
                                contact.getId(),
                                contact.getFullName(),
                                contact.getPhoneNumber(),
                                contact.getRelationship()))
                        .toList(),
                patient.getRegistrationHospitalId(),
                patient.getRegistrationHospitalCode(),
                patient.isActive(),
                patient.getCreatedAt());
    }

    private String nextPatientCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = "PAT-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (!patientRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un numéro de dossier patient unique.");
    }

    private String nextNationalIdentifier() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String identifier = "NAT-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
            if (!patientRepository.existsByNationalIdentifierIgnoreCase(identifier)) {
                return identifier;
            }
        }
        throw new IllegalStateException("Impossible de générer un identifiant national patient unique.");
    }

    private void assertNoDuplicate(CreatePatientRequest request) {
        if (!findDuplicateCandidates(
                request.firstName(),
                request.lastName(),
                request.middleName(),
                request.dateOfBirth(),
                request.gender()).isEmpty()) {
            throw new DuplicatePatientException("cette identité");
        }
    }

    private List<PatientEntity> findDuplicateCandidates(
            String firstName,
            String lastName,
            String middleName,
            LocalDate dateOfBirth,
            Gender gender) {
        String normalizedMiddleName = trimToNull(middleName);
        return patientRepository.findByIdentity(
                lastName.trim(),
                firstName.trim(),
                normalizedMiddleName == null ? "" : normalizedMiddleName,
                dateOfBirth,
                gender);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeSearchFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private void assertAccess(DataAccessScope accessScope, String hospitalCode) {
        if (!accessScope.canAccessHospital(hospitalCode)) {
            throw new DataAccessDeniedException();
        }
    }
}
