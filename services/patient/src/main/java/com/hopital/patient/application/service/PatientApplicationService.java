package com.hopital.patient.application.service;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.Gender;
import com.hopital.patient.application.domain.PatientAuditEventType;
import com.hopital.patient.application.domain.PatientDocumentType;
import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import com.hopital.patient.application.dto.CreatePatientRequest;
import com.hopital.patient.application.dto.CreatePatientDocumentRequest;
import com.hopital.patient.application.dto.CreatePatientPassageRequest;
import com.hopital.patient.application.dto.AssignPatientPassageResponsiblePersonnelRequest;
import com.hopital.patient.application.dto.EmergencyContactResponse;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.PatientDuplicateCheckRequest;
import com.hopital.patient.application.dto.PatientDuplicateCheckResponse;
import com.hopital.patient.application.dto.PatientAuditEventResponse;
import com.hopital.patient.application.dto.PatientDocumentResponse;
import com.hopital.patient.application.dto.PatientResponse;
import com.hopital.patient.application.dto.PatientPassageResponse;
import com.hopital.patient.application.dto.PatientPassageSummaryResponse;
import com.hopital.patient.application.dto.PatientSummaryResponse;
import com.hopital.patient.application.dto.UpdatePatientStatusRequest;
import com.hopital.patient.application.dto.UpdatePatientRequest;
import com.hopital.patient.application.dto.UpdatePatientPassageStatusRequest;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.exception.DuplicatePatientException;
import com.hopital.patient.application.exception.InvalidPatientDocumentException;
import com.hopital.patient.application.exception.InvalidPatientPassageStateException;
import com.hopital.patient.application.exception.PatientNotFoundException;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.integration.personnel.PersonnelReferenceClient;
import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.infra.persistence.entity.PatientDocumentEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassageEntity;
import com.hopital.patient.infra.persistence.repository.PatientPassageRepository;
import com.hopital.patient.infra.persistence.repository.PatientDocumentRepository;
import com.hopital.patient.infra.persistence.repository.PatientRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PatientApplicationService {

    private static final int MAX_DOCUMENT_SIZE_BYTES = 2 * 1024 * 1024;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> DOCUMENT_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png",
            "image/webp");

    private final PatientRepository patientRepository;
    private final PatientDocumentRepository patientDocumentRepository;
    private final PatientPassageRepository patientPassageRepository;
    private final HospitalReferenceClient hospitalReferenceClient;
    private final PersonnelReferenceClient personnelReferenceClient;

    public PatientApplicationService(
            PatientRepository patientRepository,
            PatientDocumentRepository patientDocumentRepository,
            PatientPassageRepository patientPassageRepository,
            HospitalReferenceClient hospitalReferenceClient,
            PersonnelReferenceClient personnelReferenceClient) {
        this.patientRepository = patientRepository;
        this.patientDocumentRepository = patientDocumentRepository;
        this.patientPassageRepository = patientPassageRepository;
        this.hospitalReferenceClient = hospitalReferenceClient;
        this.personnelReferenceClient = personnelReferenceClient;
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

    /**
     * Documents are loaded separately from the patient dossier so base64 file contents never bloat
     * the administrative record or the patient register.
     */
    public PageResponse<PatientDocumentResponse> searchDocuments(
            UUID patientId,
            int page,
            int size,
            String query,
            PatientDocumentType documentType,
            DataAccessScope accessScope) {
        PatientEntity patient = getPatientForScope(patientId, accessScope);
        var documents = patientDocumentRepository.search(
                patient.getId(),
                normalizeSearchFilter(query),
                documentType,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("createdAt").descending()));
        return new PageResponse<>(
                documents.getContent().stream().map(this::toDocument).toList(),
                documents.getNumber(),
                documents.getSize(),
                documents.getTotalElements(),
                documents.getTotalPages());
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

    /**
     * Returns the administrative view of one passage. The passage identifier is
     * globally unique, but access is still constrained to the caller's hospital
     * (or to the whole province for authorized administrators).
     */
    public PatientPassageSummaryResponse getPassage(UUID passageId, DataAccessScope accessScope) {
        PatientPassageEntity passage = patientPassageRepository.findById(passageId)
                .orElseThrow(() -> new PatientNotFoundException(passageId.toString()));
        assertAccess(accessScope, passage.getPatient().getRegistrationHospitalCode());
        return toPassageSummary(passage);
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
        if (request.responsiblePersonnelId() != null) {
            assignResponsiblePersonnel(passage, request.responsiblePersonnelId(), auditActor);
        }
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
        if (request.status() == PatientPassageStatus.CLOSED && passage.getResponsiblePersonnelId() == null) {
            throw new InvalidPatientPassageStateException(
                    "Un personnel responsable doit être affecté avant de terminer le passage.");
        }
        if (passage.getStatus() != request.status()) {
            passage.changeStatus(request.status(), auditActor, Instant.now());
        }
        return toPassage(passage);
    }

    @Transactional
    public PatientPassageResponse assignPassageResponsiblePersonnel(
            UUID patientId,
            UUID passageId,
            AssignPatientPassageResponsiblePersonnelRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientPassageEntity passage = patientPassageRepository.findById(passageId)
                .orElseThrow(() -> new PatientNotFoundException(passageId.toString()));
        if (!passage.getPatient().getId().equals(patientId)) {
            throw new PatientNotFoundException(passageId.toString());
        }
        assertAccess(accessScope, passage.getPatient().getRegistrationHospitalCode());
        if (passage.getStatus() != PatientPassageStatus.OPEN) {
            throw new InvalidPatientPassageStateException(
                    "Le personnel responsable ne peut être modifié que sur un passage en cours.");
        }
        assignResponsiblePersonnel(passage, request.personnelId(), auditActor);
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

    @Transactional
    public PatientDocumentResponse addDocument(
            UUID patientId,
            CreatePatientDocumentRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientEntity patient = getPatientForScope(patientId, accessScope);
        DocumentContent content = validateDocument(request);
        if (request.documentType() == PatientDocumentType.PROFILE_PHOTO) {
            patientDocumentRepository.deleteByPatientIdAndDocumentType(patient.getId(), request.documentType());
        }
        Instant createdAt = Instant.now();
        PatientDocumentEntity document = new PatientDocumentEntity(
                UUID.randomUUID(),
                patient.getId(),
                request.documentType(),
                request.fileName().trim(),
                content.contentType(),
                content.sizeBytes(),
                content.contentBase64(),
                createdAt,
                auditActor.userId(),
                auditActor.username());
        PatientDocumentResponse response = toDocument(patientDocumentRepository.save(document));
        patient.recordModification(
                auditActor,
                PatientAuditEventType.DOCUMENT_ADDED,
                "Pièce ajoutée au dossier : " + documentTypeLabel(request.documentType()) + ".",
                createdAt);
        return response;
    }

    @Transactional
    public void deleteDocument(
            UUID patientId,
            UUID documentId,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientEntity patient = getPatientForScope(patientId, accessScope);
        PatientDocumentEntity document = patientDocumentRepository.findByIdAndPatientId(documentId, patient.getId())
                .orElseThrow(() -> new PatientNotFoundException(documentId.toString()));
        patientDocumentRepository.delete(document);
        patient.recordModification(
                auditActor,
                PatientAuditEventType.DOCUMENT_REMOVED,
                "Pièce retirée du dossier : " + documentTypeLabel(document.getDocumentType()) + ".",
                Instant.now());
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

    private PatientDocumentResponse toDocument(PatientDocumentEntity document) {
        return new PatientDocumentResponse(
                document.getId(),
                document.getDocumentType(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getCreatedAt(),
                document.getCreatedByUsername(),
                document.getContentBase64());
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

    private PatientEntity getPatientForScope(UUID patientId, DataAccessScope accessScope) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        return patient;
    }

    private DocumentContent validateDocument(CreatePatientDocumentRequest request) {
        String contentType = request.contentType().trim().toLowerCase(Locale.ROOT);
        boolean imageOnly = request.documentType() == PatientDocumentType.PROFILE_PHOTO;
        Set<String> acceptedContentTypes = imageOnly ? IMAGE_CONTENT_TYPES : DOCUMENT_CONTENT_TYPES;
        if (!acceptedContentTypes.contains(contentType)) {
            throw new InvalidPatientDocumentException(imageOnly
                    ? "La photo du patient doit être au format JPEG, PNG ou WebP."
                    : "Les documents doivent être au format PDF, Word, JPEG, PNG ou WebP.");
        }

        String contentBase64 = request.contentBase64().replaceAll("\\s", "");
        if (contentBase64.startsWith("data:")) {
            throw new InvalidPatientDocumentException("Le contenu du fichier est invalide.");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(contentBase64);
        } catch (IllegalArgumentException exception) {
            throw new InvalidPatientDocumentException("Le contenu du fichier est invalide.");
        }
        if (decoded.length == 0 || decoded.length > MAX_DOCUMENT_SIZE_BYTES) {
            throw new InvalidPatientDocumentException("Chaque fichier ne peut pas dépasser 2 Mo.");
        }
        return new DocumentContent(contentType, decoded.length, contentBase64);
    }

    private String documentTypeLabel(PatientDocumentType documentType) {
        return switch (documentType) {
            case PROFILE_PHOTO -> "photo du patient";
            case IDENTITY_CARD -> "carte d’identité";
            case PASSPORT -> "passeport";
            case BIRTH_CERTIFICATE -> "acte de naissance";
            case HEALTH_INSURANCE -> "assurance ou prise en charge";
            case REFERRAL_LETTER -> "lettre de référence";
            case OTHER -> "autre pièce";
        };
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
                passage.getClosedByUsername(),
                passage.getResponsiblePersonnelId(),
                passage.getResponsiblePersonnelEmployeeNumber(),
                passage.getResponsiblePersonnelName(),
                passage.getResponsiblePersonnelJobTitle(),
                passage.getResponsibleAssignedAt(),
                passage.getResponsibleAssignedByUsername());
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
                passage.getClosedByUsername(),
                passage.getResponsiblePersonnelId(),
                passage.getResponsiblePersonnelEmployeeNumber(),
                passage.getResponsiblePersonnelName(),
                passage.getResponsiblePersonnelJobTitle(),
                passage.getResponsibleAssignedAt(),
                passage.getResponsibleAssignedByUsername());
    }

    private void assignResponsiblePersonnel(
            PatientPassageEntity passage,
            UUID personnelId,
            AuditActor auditActor) {
        PersonnelReferenceClient.PersonnelReference personnel = personnelReferenceClient
                .resolveActivePersonnelForHospital(personnelId, passage.getHospitalId());
        String fullName = java.util.stream.Stream.of(personnel.lastName(), personnel.firstName(), personnel.middleName())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
        passage.assignResponsiblePersonnel(
                personnel.id(),
                personnel.employeeNumber(),
                fullName,
                personnel.jobTitle(),
                auditActor,
                Instant.now());
    }

    private record DocumentContent(String contentType, int sizeBytes, String contentBase64) {
    }
}
