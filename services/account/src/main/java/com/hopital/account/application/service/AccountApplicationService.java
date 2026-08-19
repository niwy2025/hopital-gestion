package com.hopital.account.application.service;

import com.hopital.account.application.config.AccountSeedProperties;
import com.hopital.account.application.domain.AccountUser;
import com.hopital.account.application.domain.Role;
import com.hopital.account.application.dto.AccountResponse;
import com.hopital.account.application.dto.AuthenticatedAccountResponse;
import com.hopital.account.application.dto.CreateAccountRequest;
import com.hopital.account.application.dto.CredentialsValidationRequest;
import com.hopital.account.application.dto.PermissionResponse;
import com.hopital.account.application.dto.RoleResponse;
import com.hopital.account.application.exception.AccountNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AccountApplicationService {

    private final RolePermissionService rolePermissionService;
    private final List<AccountUser> accounts = new ArrayList<>();

    public AccountApplicationService(RolePermissionService rolePermissionService, AccountSeedProperties seed) {
        this.rolePermissionService = rolePermissionService;
        accounts.add(new AccountUser(
                UUID.randomUUID().toString(),
                seed.adminUsername(),
                seed.adminEmail(),
                "Administrateur Hopital",
                seed.adminPassword(),
                rolePermissionService.resolveRoles(Set.of("ADMIN"))));
    }

    public List<AccountResponse> listAccounts() {
        return accounts.stream().map(this::toResponse).toList();
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        Set<String> requestedRoles = request.roles() == null || request.roles().isEmpty() ? Set.of("PATIENT") : request.roles();
        Set<Role> roles = rolePermissionService.resolveRoles(requestedRoles);
        AccountUser account = new AccountUser(
                UUID.randomUUID().toString(),
                request.username(),
                request.email(),
                request.displayName(),
                request.password(),
                roles);
        accounts.add(account);
        return toResponse(account);
    }

    public AccountResponse findByIdentifier(String identifier) {
        return accounts.stream()
                .filter(account -> account.matchesIdentifier(identifier))
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new AccountNotFoundException(identifier));
    }

    public AuthenticatedAccountResponse validateCredentials(CredentialsValidationRequest request) {
        AccountUser account = accounts.stream()
                .filter(candidate -> candidate.matchesIdentifier(request.identifier()))
                .filter(candidate -> candidate.password().equals(request.password()))
                .findFirst()
                .orElse(null);
        return new AuthenticatedAccountResponse(account != null, account == null ? null : toResponse(account));
    }

    public Set<RoleResponse> listRoles() {
        return rolePermissionService.listRoles().stream().map(this::toRoleResponse).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private AccountResponse toResponse(AccountUser account) {
        return new AccountResponse(
                account.id(),
                account.username(),
                account.email(),
                account.displayName(),
                account.roles().stream().map(this::toRoleResponse).collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }

    private RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.code(),
                role.label(),
                role.permissions().stream()
                        .map(permission -> new PermissionResponse(permission.code(), permission.description()))
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }
}
