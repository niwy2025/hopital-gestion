package com.hopital.personnel.application.service;

import com.hopital.personnel.application.dto.CreatePersonnelRequest;
import com.hopital.personnel.application.dto.PageResponse;
import com.hopital.personnel.application.dto.PersonnelResponse;
import com.hopital.personnel.application.dto.UpdatePersonnelRequest;
import com.hopital.personnel.application.exception.DuplicatePersonnelException;
import com.hopital.personnel.application.exception.InvalidPersonnelReferenceException;
import com.hopital.personnel.application.exception.PersonnelNotFoundException;
import com.hopital.personnel.infra.persistence.entity.PersonnelEntity;
import com.hopital.personnel.infra.integration.account.AccountReferenceClient;
import com.hopital.personnel.infra.persistence.repository.PersonnelRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PersonnelApplicationService {

    private final PersonnelRepository personnelRepository;
    private final AccountReferenceClient accountReferenceClient;

    public PersonnelApplicationService(
            PersonnelRepository personnelRepository,
            AccountReferenceClient accountReferenceClient) {
        this.personnelRepository = personnelRepository;
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

    public PersonnelResponse findById(UUID personnelId) {
        return personnelRepository.findById(personnelId)
                .map(this::toResponse)
                .orElseThrow(() -> new PersonnelNotFoundException(personnelId.toString()));
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
        PersonnelEntity personnel = personnelRepository.findById(personnelId)
                .orElseThrow(() -> new PersonnelNotFoundException(personnelId.toString()));
        personnel.setActive(active);
        return toResponse(personnel);
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
