package com.hopital.account.application.service;

import com.hopital.account.application.dto.AccountResponse;
import com.hopital.account.application.dto.AccountDetailsResponse;
import com.hopital.account.application.dto.AuthenticatedAccountResponse;
import com.hopital.account.application.dto.CreateAccountRequest;
import com.hopital.account.application.dto.CredentialsValidationRequest;
import com.hopital.account.application.dto.PageResponse;
import com.hopital.account.application.dto.RoleResponse;
import com.hopital.account.application.dto.UpdateAccountRequest;
import com.hopital.account.application.domain.AccountCreatedEvent;
import com.hopital.account.application.exception.DuplicateAccountException;
import com.hopital.account.application.exception.InvalidHospitalAssignmentException;
import com.hopital.account.application.exception.InvalidProfilePhotoException;
import com.hopital.account.application.exception.AccountNotFoundException;
import com.hopital.account.infra.persistence.entity.AccountEntity;
import com.hopital.account.infra.persistence.repository.AccountRepository;
import java.util.List;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountApplicationService {

    private final RolePermissionService rolePermissionService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountApplicationService(
            RolePermissionService rolePermissionService,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher applicationEventPublisher) {
        this.rolePermissionService = rolePermissionService;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public List<AccountResponse> listAccounts() {
        return accountRepository.findAllByOrderByDisplayNameAsc().stream().map(this::toResponse).toList();
    }

    public PageResponse<AccountResponse> searchAccounts(int page, int size, String query, String hospitalId) {
        var accounts = accountRepository.search(
                normalizeSearchFilter(query),
                parseOptionalUuid(hospitalId),
                PageRequest.of(normalizePage(page), normalizePageSize(size), Sort.by("displayName").ascending()));
        return new PageResponse<>(
                accounts.getContent().stream().map(this::toResponse).toList(),
                accounts.getNumber(),
                accounts.getSize(),
                accounts.getTotalElements(),
                accounts.getTotalPages());
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        String username = request.username().trim();
        String email = request.email().trim();
        if (accountRepository.existsByUsernameIgnoreCaseOrEmailIgnoreCase(username, email)) {
            throw new DuplicateAccountException("cet identifiant ou cette adresse e-mail", username + " / " + email);
        }
        Set<String> requestedRoles = request.roles() == null || request.roles().isEmpty() ? Set.of("PATIENT") : request.roles();
        ProfilePhoto profilePhoto = parseProfilePhoto(request.profilePhotoBase64(), request.profilePhotoContentType());
        AccountEntity account = new AccountEntity(
                UUID.randomUUID(),
                username,
                email,
                request.displayName().trim(),
                passwordEncoder.encode(request.password()),
                parseOptionalUuid(request.hospitalId()),
                profilePhoto == null ? null : profilePhoto.base64(),
                profilePhoto == null ? null : profilePhoto.contentType(),
                rolePermissionService.resolveRoles(requestedRoles));
        AccountResponse response = toResponse(accountRepository.save(account));
        applicationEventPublisher.publishEvent(new AccountCreatedEvent(response));
        return response;
    }

    public AccountDetailsResponse findById(UUID accountId) {
        return accountRepository.findById(accountId)
                .map(this::toDetailsResponse)
                .orElseThrow(() -> new AccountNotFoundException(accountId.toString()));
    }

    public AccountResponse findSummaryById(UUID accountId) {
        return accountRepository.findById(accountId)
                .map(this::toResponse)
                .orElseThrow(() -> new AccountNotFoundException(accountId.toString()));
    }

    @Transactional
    public AccountDetailsResponse updateAccount(UUID accountId, UpdateAccountRequest request) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId.toString()));
        String username = request.username().trim();
        String email = request.email().trim();
        if (accountRepository.existsByUsernameIgnoreCaseAndIdNot(username, accountId)) {
            throw new DuplicateAccountException("l’identifiant", username);
        }
        if (accountRepository.existsByEmailIgnoreCaseAndIdNot(email, accountId)) {
            throw new DuplicateAccountException("l’adresse e-mail", email);
        }

        account.updateProfile(
                username,
                email,
                request.displayName().trim(),
                parseOptionalUuid(request.hospitalId()),
                rolePermissionService.resolveRoles(request.roles()));
        if (request.password() != null && !request.password().isBlank()) {
            account.changePassword(passwordEncoder.encode(request.password()));
        }
        if (request.removeProfilePhoto()) {
            account.removeProfilePhoto();
        } else {
            ProfilePhoto profilePhoto = parseProfilePhoto(request.profilePhotoBase64(), request.profilePhotoContentType());
            if (profilePhoto != null) {
                account.changeProfilePhoto(profilePhoto.base64(), profilePhoto.contentType());
            }
        }
        return toDetailsResponse(account);
    }

    public AccountResponse findByIdentifier(String identifier) {
        return accountRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier)
                .map(this::toResponse)
                .orElseThrow(() -> new AccountNotFoundException(identifier));
    }

    public AuthenticatedAccountResponse validateCredentials(CredentialsValidationRequest request) {
        AccountEntity account = accountRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(request.identifier(), request.identifier())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .filter(this::isAllowedToSignIn)
                .orElse(null);
        return new AuthenticatedAccountResponse(account != null, account == null ? null : toResponse(account));
    }

    public Set<RoleResponse> listRoles() {
        return rolePermissionService.listRoles();
    }

    private AccountResponse toResponse(AccountEntity account) {
        return new AccountResponse(
                account.getId().toString(),
                account.getUsername(),
                account.getEmail(),
                account.getDisplayName(),
                account.getHospitalId() == null ? null : account.getHospitalId().toString(),
                account.getRoles().stream()
                        .map(rolePermissionService::toResponse)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }

    private AccountDetailsResponse toDetailsResponse(AccountEntity account) {
        AccountResponse accountResponse = toResponse(account);
        return new AccountDetailsResponse(
                accountResponse.id(),
                accountResponse.username(),
                accountResponse.email(),
                accountResponse.displayName(),
                accountResponse.hospitalId(),
                accountResponse.roles(),
                account.getProfilePhotoBase64(),
                account.getProfilePhotoContentType());
    }

    private boolean isAllowedToSignIn(AccountEntity account) {
        return account.getHospitalId() != null
                || account.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getCode()));
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizePageSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private String normalizeSearchFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private UUID parseOptionalUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new InvalidHospitalAssignmentException(value);
        }
    }

    private ProfilePhoto parseProfilePhoto(String base64, String contentType) {
        if (base64 == null && contentType == null) {
            return null;
        }
        if (base64 == null || base64.isBlank() || contentType == null || contentType.isBlank()) {
            throw new InvalidProfilePhotoException("La photo de profil est incomplète.");
        }

        String normalizedContentType = contentType.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("image/jpeg", "image/png", "image/webp").contains(normalizedContentType)) {
            throw new InvalidProfilePhotoException("Le format de la photo doit être JPEG, PNG ou WebP.");
        }

        try {
            byte[] photoBytes = Base64.getDecoder().decode(base64);
            if (photoBytes.length == 0 || photoBytes.length > 512 * 1024) {
                throw new InvalidProfilePhotoException("La photo de profil ne doit pas dépasser 512 Ko.");
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidProfilePhotoException("Le contenu de la photo de profil est invalide.");
        }
        return new ProfilePhoto(base64, normalizedContentType);
    }

    private record ProfilePhoto(String base64, String contentType) {
    }
}
