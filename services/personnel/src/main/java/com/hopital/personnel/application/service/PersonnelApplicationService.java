package com.hopital.personnel.application.service;

import com.hopital.personnel.application.domain.PersonnelDocumentType;
import com.hopital.personnel.application.domain.PersonnelAssignmentStatus;
import com.hopital.personnel.application.domain.PersonnelAssignmentScope;
import com.hopital.personnel.application.dto.ClosePersonnelAssignmentRequest;
import com.hopital.personnel.application.dto.CreatePersonnelAssignmentRequest;
import com.hopital.personnel.application.dto.CreatePersonnelDocumentRequest;
import com.hopital.personnel.application.dto.CreatePersonnelRequest;
import com.hopital.personnel.application.dto.PageResponse;
import com.hopital.personnel.application.dto.PersonnelDetailsResponse;
import com.hopital.personnel.application.dto.PersonnelAssignmentResponse;
import com.hopital.personnel.application.dto.PersonnelDocumentResponse;
import com.hopital.personnel.application.dto.PersonnelResponse;
import com.hopital.personnel.application.dto.UpdatePersonnelRequest;
import com.hopital.personnel.application.exception.DuplicatePersonnelException;
import com.hopital.personnel.application.exception.InvalidPersonnelDocumentException;
import com.hopital.personnel.application.exception.InvalidPersonnelAssignmentException;
import com.hopital.personnel.application.exception.InvalidPersonnelReferenceException;
import com.hopital.personnel.application.exception.PersonnelNotFoundException;
import com.hopital.personnel.infra.integration.account.AccountReferenceClient;
import com.hopital.personnel.infra.persistence.entity.PersonnelDocumentEntity;
import com.hopital.personnel.infra.persistence.entity.PersonnelAssignmentEntity;
import com.hopital.personnel.infra.persistence.entity.PersonnelEntity;
import com.hopital.personnel.infra.persistence.repository.PersonnelDocumentRepository;
import com.hopital.personnel.infra.persistence.repository.PersonnelAssignmentRepository;
import com.hopital.personnel.infra.persistence.repository.PersonnelRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.EnumSet;
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
public class PersonnelApplicationService {

    private static final int MAX_DOCUMENT_SIZE_BYTES = 2 * 1024 * 1024;
    private static final Set<PersonnelDocumentType> REPLACEABLE_DOCUMENT_TYPES = EnumSet.of(
            PersonnelDocumentType.PROFILE_PHOTO,
            PersonnelDocumentType.SIGNATURE,
            PersonnelDocumentType.CV);
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> DOCUMENT_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png",
            "image/webp");

    private final PersonnelRepository personnelRepository;
    private final PersonnelAssignmentRepository personnelAssignmentRepository;
    private final PersonnelDocumentRepository personnelDocumentRepository;
    private final AccountReferenceClient accountReferenceClient;

    public PersonnelApplicationService(
            PersonnelRepository personnelRepository,
            PersonnelAssignmentRepository personnelAssignmentRepository,
            PersonnelDocumentRepository personnelDocumentRepository,
            AccountReferenceClient accountReferenceClient) {
        this.personnelRepository = personnelRepository;
        this.personnelAssignmentRepository = personnelAssignmentRepository;
        this.personnelDocumentRepository = personnelDocumentRepository;
        this.accountReferenceClient = accountReferenceClient;
    }

    public PageResponse<PersonnelResponse> searchPersonnel(int page, int size, String query, String hospitalId, Boolean active) {
        var results = personnelRepository.search(
                normalizeSearchFilter(query),
                parseOptionalUuid(hospitalId, "hôpital"),
                active,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                        Sort.by("lastName").ascending().and(Sort.by("firstName").ascending())));
        return new PageResponse<>(
                results.getContent().stream().map(this::toResponse).toList(),
                results.getNumber(),
                results.getSize(),
                results.getTotalElements(),
                results.getTotalPages());
    }

    public PersonnelDetailsResponse findById(UUID personnelId) {
        PersonnelEntity personnel = getPersonnel(personnelId);
        List<PersonnelDocumentResponse> documents = personnelDocumentRepository.findByPersonnelIdOrderByCreatedAtDesc(personnelId)
                .stream()
                .map(this::toDocumentResponse)
                .toList();
        return toDetailsResponse(personnel, documents);
    }

    public PersonnelResponse findByAccountId(UUID accountId) {
        return personnelRepository.findByAccountId(accountId)
                .map(this::toResponse)
                .orElseThrow(() -> new PersonnelNotFoundException("associé au compte " + accountId));
    }

    @Transactional
    public PersonnelResponse createPersonnel(CreatePersonnelRequest request) {
        String employeeNumber = normalizeEmployeeNumber(request.employeeNumber());
        if (personnelRepository.existsByEmployeeNumberIgnoreCase(employeeNumber)) {
            throw new DuplicatePersonnelException("Ce matricule est déjà attribué.");
        }
        UUID accountId = parseOptionalUuid(request.accountId(), "compte utilisateur");
        assertAvailableAccount(accountId, null);
        assertExistingAccount(accountId);
        PersonnelEntity personnel = new PersonnelEntity(
                UUID.randomUUID(),
                employeeNumber,
                request.firstName().trim(),
                request.lastName().trim(),
                trimToNull(request.middleName()),
                request.dateOfBirth(),
                request.gender(),
                request.category(),
                request.jobTitle().trim(),
                trimToNull(request.phoneNumber()),
                trimToNull(request.email()),
                trimToNull(request.address()),
                parseOptionalUuid(request.hospitalId(), "hôpital"),
                accountId,
                Instant.now());
        return toResponse(personnelRepository.save(personnel));
    }

    @Transactional
    public PersonnelResponse updatePersonnel(UUID personnelId, UpdatePersonnelRequest request) {
        PersonnelEntity personnel = personnelRepository.findById(personnelId)
                .orElseThrow(() -> new PersonnelNotFoundException(personnelId.toString()));
        String employeeNumber = normalizeEmployeeNumber(request.employeeNumber());
        if (personnelRepository.existsByEmployeeNumberIgnoreCaseAndIdNot(employeeNumber, personnelId)) {
            throw new DuplicatePersonnelException("Ce matricule est déjà attribué.");
        }
        UUID accountId = parseOptionalUuid(request.accountId(), "compte utilisateur");
        assertAvailableAccount(accountId, personnelId);
        assertExistingAccount(accountId);
        personnel.update(
                employeeNumber,
                request.firstName().trim(),
                request.lastName().trim(),
                trimToNull(request.middleName()),
                request.dateOfBirth(),
                request.gender(),
                request.category(),
                request.jobTitle().trim(),
                trimToNull(request.phoneNumber()),
                trimToNull(request.email()),
                trimToNull(request.address()),
                parseOptionalUuid(request.hospitalId(), "hôpital"),
                accountId);
        return toResponse(personnel);
    }

    @Transactional
    public PersonnelResponse updateStatus(UUID personnelId, boolean active) {
        PersonnelEntity personnel = getPersonnel(personnelId);
        personnel.setActive(active);
        return toResponse(personnel);
    }

    public PageResponse<PersonnelAssignmentResponse> searchAssignments(
            UUID personnelId, int page, int size, String query, PersonnelAssignmentStatus status) {
        getPersonnel(personnelId);
        var assignments = personnelAssignmentRepository.search(
                personnelId,
                normalizeSearchFilter(query),
                status,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                        Sort.by("startsOn").descending()));
        return new PageResponse<>(
                assignments.getContent().stream().map(this::toAssignmentResponse).toList(),
                assignments.getNumber(), assignments.getSize(), assignments.getTotalElements(), assignments.getTotalPages());
    }

    @Transactional
    public PersonnelAssignmentResponse createAssignment(UUID personnelId, CreatePersonnelAssignmentRequest request) {
        PersonnelEntity personnel = getPersonnel(personnelId);
        if (!personnel.isActive()) {
            throw new InvalidPersonnelAssignmentException("Un agent inactif ne peut pas recevoir une nouvelle affectation.");
        }
        UUID hospitalId = parseOptionalUuid(request.hospitalId(), "hôpital");
        validateAssignmentScope(request.scope(), hospitalId);
        if (request.primaryAssignment() && personnelAssignmentRepository
                .existsByPersonnelIdAndStatusAndPrimaryAssignmentTrue(personnelId, PersonnelAssignmentStatus.ACTIVE)) {
            throw new InvalidPersonnelAssignmentException("Cet agent possède déjà une affectation principale active. Clôturez-la avant d'en définir une autre.");
        }
        PersonnelAssignmentEntity assignment = new PersonnelAssignmentEntity(
                UUID.randomUUID(), personnelId, request.scope(), hospitalId,
                trimToNull(request.departmentName()), trimToNull(request.unitName()), request.positionTitle().trim(),
                request.startsOn(), request.primaryAssignment(), trimToNull(request.notes()), Instant.now());
        return toAssignmentResponse(personnelAssignmentRepository.save(assignment));
    }

    @Transactional
    public PersonnelAssignmentResponse closeAssignment(
            UUID personnelId, UUID assignmentId, ClosePersonnelAssignmentRequest request) {
        PersonnelAssignmentEntity assignment = personnelAssignmentRepository.findByIdAndPersonnelId(assignmentId, personnelId)
                .orElseThrow(() -> new PersonnelNotFoundException("affectation " + assignmentId));
        if (assignment.getStatus() == PersonnelAssignmentStatus.ENDED) {
            throw new InvalidPersonnelAssignmentException("Cette affectation est déjà clôturée.");
        }
        if (request.endsOn().isBefore(assignment.getStartsOn())) {
            throw new InvalidPersonnelAssignmentException("La date de fin ne peut pas être antérieure à la date de début.");
        }
        assignment.close(request.endsOn());
        return toAssignmentResponse(assignment);
    }

    @Transactional
    public PersonnelDocumentResponse addDocument(UUID personnelId, CreatePersonnelDocumentRequest request) {
        getPersonnel(personnelId);
        DocumentContent content = validateDocument(request);
        if (REPLACEABLE_DOCUMENT_TYPES.contains(request.documentType())) {
            personnelDocumentRepository.deleteByPersonnelIdAndDocumentType(personnelId, request.documentType());
        }
        PersonnelDocumentEntity document = new PersonnelDocumentEntity(
                UUID.randomUUID(),
                personnelId,
                request.documentType(),
                request.fileName().trim(),
                content.contentType(),
                content.sizeBytes(),
                content.contentBase64(),
                Instant.now());
        return toDocumentResponse(personnelDocumentRepository.save(document));
    }

    @Transactional
    public void deleteDocument(UUID personnelId, UUID documentId) {
        PersonnelDocumentEntity document = personnelDocumentRepository.findByIdAndPersonnelId(documentId, personnelId)
                .orElseThrow(() -> new PersonnelNotFoundException("document " + documentId));
        personnelDocumentRepository.delete(document);
    }

    private void assertAvailableAccount(UUID accountId, UUID personnelId) {
        if (accountId == null) {
            return;
        }
        boolean alreadyAssociated = personnelId == null
                ? personnelRepository.existsByAccountId(accountId)
                : personnelRepository.existsByAccountIdAndIdNot(accountId, personnelId);
        if (alreadyAssociated) {
            throw new DuplicatePersonnelException("Ce compte utilisateur est déjà associé à un autre membre du personnel.");
        }
    }

    private void assertExistingAccount(UUID accountId) {
        if (accountId != null) {
            accountReferenceClient.assertAccountExists(accountId);
        }
    }

    private PersonnelResponse toResponse(PersonnelEntity personnel) {
        return new PersonnelResponse(
                personnel.getId(),
                personnel.getEmployeeNumber(),
                personnel.getFirstName(),
                personnel.getLastName(),
                personnel.getMiddleName(),
                personnel.getDateOfBirth(),
                personnel.getGender(),
                personnel.getCategory(),
                personnel.getJobTitle(),
                personnel.getPhoneNumber(),
                personnel.getEmail(),
                personnel.getAddress(),
                personnel.getHospitalId(),
                personnel.getAccountId(),
                personnel.isActive(),
                personnel.getCreatedAt());
    }

    private PersonnelDetailsResponse toDetailsResponse(
            PersonnelEntity personnel,
            List<PersonnelDocumentResponse> documents) {
        return new PersonnelDetailsResponse(
                personnel.getId(),
                personnel.getEmployeeNumber(),
                personnel.getFirstName(),
                personnel.getLastName(),
                personnel.getMiddleName(),
                personnel.getDateOfBirth(),
                personnel.getGender(),
                personnel.getCategory(),
                personnel.getJobTitle(),
                personnel.getPhoneNumber(),
                personnel.getEmail(),
                personnel.getAddress(),
                personnel.getHospitalId(),
                personnel.getAccountId(),
                personnel.isActive(),
                personnel.getCreatedAt(),
                documents);
    }

    private PersonnelDocumentResponse toDocumentResponse(PersonnelDocumentEntity document) {
        return new PersonnelDocumentResponse(
                document.getId(),
                document.getDocumentType(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getCreatedAt(),
                document.getContentBase64());
    }

    private PersonnelAssignmentResponse toAssignmentResponse(PersonnelAssignmentEntity assignment) {
        return new PersonnelAssignmentResponse(
                assignment.getId(), assignment.getPersonnelId(), assignment.getScope(), assignment.getHospitalId(),
                assignment.getDepartmentName(), assignment.getUnitName(), assignment.getPositionTitle(),
                assignment.getStartsOn(), assignment.getEndsOn(), assignment.getStatus(), assignment.isPrimaryAssignment(),
                assignment.getNotes(), assignment.getCreatedAt());
    }

    private PersonnelEntity getPersonnel(UUID personnelId) {
        return personnelRepository.findById(personnelId)
                .orElseThrow(() -> new PersonnelNotFoundException(personnelId.toString()));
    }

    private DocumentContent validateDocument(CreatePersonnelDocumentRequest request) {
        String contentType = request.contentType().trim().toLowerCase(Locale.ROOT);
        boolean isImageOnlyDocument = request.documentType() == PersonnelDocumentType.PROFILE_PHOTO
                || request.documentType() == PersonnelDocumentType.SIGNATURE;
        Set<String> acceptedContentTypes = isImageOnlyDocument ? IMAGE_CONTENT_TYPES : DOCUMENT_CONTENT_TYPES;
        if (!acceptedContentTypes.contains(contentType)) {
            throw new InvalidPersonnelDocumentException(isImageOnlyDocument
                    ? "La photo et la signature doivent être au format JPEG, PNG ou WebP."
                    : "Les documents doivent être au format PDF, Word, JPEG, PNG ou WebP.");
        }

        String contentBase64 = request.contentBase64().replaceAll("\\s", "");
        if (contentBase64.startsWith("data:")) {
            throw new InvalidPersonnelDocumentException("Le contenu du fichier est invalide.");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(contentBase64);
        } catch (IllegalArgumentException exception) {
            throw new InvalidPersonnelDocumentException("Le contenu du fichier est invalide.");
        }
        if (decoded.length == 0 || decoded.length > MAX_DOCUMENT_SIZE_BYTES) {
            throw new InvalidPersonnelDocumentException("Chaque fichier ne peut pas dépasser 2 Mo.");
        }
        return new DocumentContent(contentType, decoded.length, contentBase64);
    }

    private void validateAssignmentScope(PersonnelAssignmentScope scope, UUID hospitalId) {
        if (scope == PersonnelAssignmentScope.HOSPITAL && hospitalId == null) {
            throw new InvalidPersonnelAssignmentException("Une affectation hospitalière doit indiquer un hôpital.");
        }
        if (scope == PersonnelAssignmentScope.PROVINCIAL && hospitalId != null) {
            throw new InvalidPersonnelAssignmentException("Une affectation provinciale ne peut pas être liée à un hôpital.");
        }
    }

    private record DocumentContent(String contentType, int sizeBytes, String contentBase64) {
    }

    private UUID parseOptionalUuid(String value, String label) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return UUID.fromString(normalized);
        } catch (IllegalArgumentException exception) {
            throw new InvalidPersonnelReferenceException("La référence de " + label + " est invalide.");
        }
    }

    private String normalizeEmployeeNumber(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeSearchFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
