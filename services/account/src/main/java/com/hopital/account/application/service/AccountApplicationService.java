package com.hopital.account.application.service;

import com.hopital.account.application.dto.AccountResponse;
import com.hopital.account.application.dto.AuthenticatedAccountResponse;
import com.hopital.account.application.dto.CreateAccountRequest;
import com.hopital.account.application.dto.CredentialsValidationRequest;
import com.hopital.account.application.dto.RoleResponse;
import com.hopital.account.application.domain.AccountCreatedEvent;
import com.hopital.account.application.exception.AccountNotFoundException;
import com.hopital.account.infra.persistence.entity.AccountEntity;
import com.hopital.account.infra.persistence.repository.AccountRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
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
        return accountRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Set<String> requestedRoles = request.roles() == null || request.roles().isEmpty() ? Set.of("PATIENT") : request.roles();
        AccountEntity account = new AccountEntity(
                UUID.randomUUID(),
                request.username(),
                request.email(),
                request.displayName(),
                passwordEncoder.encode(request.password()),
                rolePermissionService.resolveRoles(requestedRoles));
        AccountResponse response = toResponse(accountRepository.save(account));
        applicationEventPublisher.publishEvent(new AccountCreatedEvent(response));
        return response;
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
                account.getRoles().stream()
                        .map(rolePermissionService::toResponse)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }
}
