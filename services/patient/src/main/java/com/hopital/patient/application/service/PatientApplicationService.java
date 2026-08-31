package com.hopital.patient.application.service;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.Gender;
import com.hopital.patient.application.domain.PatientAuditEventType;
import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import com.hopital.patient.application.dto.CreatePatientRequest;
import com.hopital.patient.application.dto.CreatePatientPassageRequest;
import com.hopital.patient.application.dto.EmergencyContactResponse;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.PatientDuplicateCheckRequest;
import com.hopital.patient.application.dto.PatientDuplicateCheckResponse;
import com.hopital.patient.application.dto.PatientAuditEventResponse;
import com.hopital.patient.application.dto.PatientResponse;
import com.hopital.patient.application.dto.PatientPassageResponse;
import com.hopital.patient.application.dto.PatientPassageSummaryResponse;
import com.hopital.patient.application.dto.PatientSummaryResponse;
import com.hopital.patient.application.dto.UpdatePatientStatusRequest;
import com.hopital.patient.application.dto.UpdatePatientRequest;
import com.hopital.patient.application.dto.UpdatePatientPassageStatusRequest;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.exception.DuplicatePatientException;
import com.hopital.patient.application.exception.PatientNotFoundException;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassageEntity;
import com.hopital.patient.infra.persistence.repository.PatientPassageRepository;
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
    private final PatientPassageRepository patientPassageRepository;
    private final HospitalReferenceClient hospitalReferenceClient;

    public PatientApplicationService(
            PatientRepository patientRepository,
            PatientPassageRepository patientPassageRepository,
            HospitalReferenceClient hospitalReferenceClient) {
        this.patientRepository = patientRepository;
        this.patientPassageRepository = patientPassageRepository;
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

    public PageResponse<PatientPassageResponse> searchPassages(
            UUID patientId,
            int page,
            int size,
            String query,
            PatientPassageType type,
            PatientPassageStatus status,
            DataAccessScope accessScope) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        var passages = patientPassageRepository.search(
                patientId,
                normalizeSearchFilter(query),
                type,
                status,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("arrivedAt").descending()));
        return new PageResponse<>(
                passages.getContent().stream().map(this::toPassage).toList(),
                passages.getNumber(),
                passages.getSize(),
                passages.getTotalElements(),
                passages.getTotalPages());
    }

    public PageResponse<PatientPassageSummaryResponse> searchPassageRegistry(
            int page,
            int size,
            String query,
            UUID hospitalId,
            PatientPassageType type,
            PatientPassageStatus status,
            DataAccessScope accessScope) {
        String scopeHospitalCode = "";
        if (!accessScope.provinceWide()) {
            if (accessScope.hospitalCode() == null || accessScope.hospitalCode().isBlank()) {
                throw new DataAccessDeniedException();
            }
            scopeHospitalCode = accessScope.hospitalCode();
        }

        var passages = patientPassageRepository.searchRegistry(
                scopeHospitalCode,
                accessScope.provinceWide() ? hospitalId : null,
                normalizeSearchFilter(query),
                type,
                status,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("arrivedAt").descending()));
        return new PageResponse<>(
                passages.getContent().stream().map(this::toPassageSummary).toList(),
                passages.getNumber(),
                passages.getSize(),
                passages.getTotalElements(),
                passages.getTotalPages());
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
    public PatientResponse createPatient(
            CreatePatientRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        HospitalReferenceClient.HospitalReference hospital = hospitalReferenceClient.resolveActiveHospital(
                request.registrationHospitalId());
        String registrationHospitalCode = normalizeCode(hospital.hospitalCode());
        assertAccess(accessScope, registrationHospitalCode);

        assertNoDuplicate(request);

        Instant createdAt = Instant.now();
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
                createdAt);
        for (int index = 0; index < request.emergencyContacts().size(); index++) {
            var contact = request.emergencyContacts().get(index);
            patient.addEmergencyContact(
                    contact.fullName().trim(),
                    contact.phoneNumber().trim(),
                    contact.relationship(),
                    index);
        }
        patient.recordCreation(auditActor, createdAt);
        return toDetails(patientRepository.save(patient));
    }

    @Transactional
    public PatientPassageResponse createPassage(
            UUID patientId,
            CreatePatientPassageRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());

        HospitalReferenceClient.HospitalReference hospital = resolvePassageHospital(request.hospitalId(), accessScope);
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(),
                nextPassageCode(),
                patient,
                hospital.hospitalId(),
                normalizeCode(hospital.hospitalCode()),
                request.type(),
                trimToNull(request.serviceName()),
                trimToNull(request.reason()),
                auditActor,
                Instant.now());
        return toPassage(patientPassageRepository.save(passage));
    }

    @Transactional
    public PatientResponse updatePatient(
            UUID patientId,
            UpdatePatientRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        assertNoDuplicate(request, patient.getId());
        patient.updateProfile(
                request.firstName().trim(),
                request.lastName().trim(),
                trimToNull(request.middleName()),
                request.dateOfBirth(),
                request.gender(),
                trimToNull(request.phoneNumber()),
                trimToNull(request.email()),
                trimToNull(request.address()));
        replaceEmergencyContacts(patient, request.emergencyContacts());
        patient.recordModification(
                auditActor,
                PatientAuditEventType.UPDATED,
                "Informations administratives mises à jour.",
                Instant.now());
        return toDetails(patient);
    }

    @Transactional
    public PatientPassageResponse updatePassageStatus(
            UUID patientId,
            UUID passageId,
            UpdatePatientPassageStatusRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientPassageEntity passage = patientPassageRepository.findById(passageId)
                .orElseThrow(() -> new PatientNotFoundException(passageId.toString()));
        if (!passage.getPatient().getId().equals(patientId)) {
            throw new PatientNotFoundException(passageId.toString());
        }
        assertAccess(accessScope, passage.getPatient().getRegistrationHospitalCode());
        if (passage.getStatus() != request.status()) {
            passage.changeStatus(request.status(), auditActor, Instant.now());
        }
        return toPassage(passage);
    }

    @Transactional
    public PatientResponse updateStatus(
            UUID patientId,
            UpdatePatientStatusRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        if (patient.isActive() != request.active()) {
            patient.setActive(request.active());
            patient.recordModification(
                    auditActor,
                    PatientAuditEventType.STATUS_CHANGED,
                    request.active() ? "Dossier patient activé." : "Dossier patient désactivé.",
                    Instant.now());
        }
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
                patient.getCreatedAt(),
                patient.getCreatedByUsername(),
                patient.getUpdatedAt(),
                patient.getUpdatedByUsername(),
                patient.getAuditEvents().stream()
                        .map(event -> new PatientAuditEventResponse(
                                event.getId(),
                                event.getType(),
                                event.getDescription(),
                                event.getOperatorUsername(),
                                event.getOccurredAt()))
                        .toList());
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

    private String nextPassageCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = "PAS-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (!patientPassageRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un numéro de passage unique.");
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

    private void assertNoDuplicate(UpdatePatientRequest request, UUID patientId) {
        boolean anotherPatientExists = findDuplicateCandidates(
                request.firstName(),
                request.lastName(),
                request.middleName(),
                request.dateOfBirth(),
                request.gender()).stream()
                .anyMatch(candidate -> !candidate.getId().equals(patientId));
        if (anotherPatientExists) {
            throw new DuplicatePatientException("cette identité");
        }
    }

    private void replaceEmergencyContacts(
            PatientEntity patient,
            List<com.hopital.patient.application.dto.EmergencyContactRequest> contacts) {
        patient.clearEmergencyContacts();
        // Hibernate may otherwise insert the new contact at order 0 before deleting
        // the existing order 0, violating uk_patient_emergency_contacts_order.
        patientRepository.flush();
        for (int index = 0; index < contacts.size(); index++) {
            var contact = contacts.get(index);
            patient.addEmergencyContact(
                    contact.fullName().trim(),
                    contact.phoneNumber().trim(),
                    contact.relationship(),
                    index);
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

    private HospitalReferenceClient.HospitalReference resolvePassageHospital(
            UUID requestedHospitalId,
            DataAccessScope accessScope) {
        if (accessScope.provinceWide()) {
            if (requestedHospitalId == null) {
                throw new IllegalArgumentException("Un hôpital est obligatoire pour enregistrer un passage.");
            }
            return hospitalReferenceClient.resolveActiveHospital(requestedHospitalId);
        }
        if (accessScope.hospitalId() == null || accessScope.hospitalCode() == null) {
            throw new DataAccessDeniedException();
        }
        return new HospitalReferenceClient.HospitalReference(
                accessScope.hospitalId(),
                accessScope.hospitalCode(),
                true);
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

    private PatientPassageResponse toPassage(PatientPassageEntity passage) {
        return new PatientPassageResponse(
                passage.getId(),
                passage.getCode(),
                passage.getHospitalId(),
                passage.getHospitalCode(),
                passage.getType(),
                passage.getServiceName(),
                passage.getReason(),
                passage.getStatus(),
                passage.getArrivedAt(),
                passage.getClosedAt(),
                passage.getCreatedByUsername(),
                passage.getClosedByUsername());
    }

    private PatientPassageSummaryResponse toPassageSummary(PatientPassageEntity passage) {
        PatientEntity patient = passage.getPatient();
        return new PatientPassageSummaryResponse(
                passage.getId(),
                passage.getCode(),
                patient.getId(),
                patient.getCode(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getMiddleName(),
                passage.getHospitalId(),
                passage.getHospitalCode(),
                passage.getType(),
                passage.getServiceName(),
                passage.getReason(),
                passage.getStatus(),
                passage.getArrivedAt(),
                passage.getClosedAt(),
                passage.getCreatedByUsername(),
                passage.getClosedByUsername());
    }
}
