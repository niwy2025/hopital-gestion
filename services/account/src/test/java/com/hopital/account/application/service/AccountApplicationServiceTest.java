package com.hopital.account.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hopital.account.application.dto.CreateAccountRequest;
import com.hopital.account.application.dto.CredentialsValidationRequest;
import com.hopital.account.application.dto.RoleResponse;
import com.hopital.account.application.domain.AccountCreatedEvent;
import com.hopital.account.infra.persistence.entity.AccountEntity;
import com.hopital.account.infra.persistence.entity.PermissionEntity;
import com.hopital.account.infra.persistence.entity.RoleEntity;
import com.hopital.account.infra.persistence.repository.AccountRepository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AccountApplicationServiceTest {

    @Mock
    private RolePermissionService rolePermissionService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Captor
    private ArgumentCaptor<AccountEntity> accountCaptor;

    @Captor
    private ArgumentCaptor<AccountCreatedEvent> accountCreatedEventCaptor;

    @InjectMocks
    private AccountApplicationService accountApplicationService;

    @Test
    void createsAnAccountWithAHashedPassword() {
        RoleEntity patientRole = new RoleEntity(
                UUID.randomUUID(),
                "PATIENT",
                "Patient",
                Set.of(new PermissionEntity(UUID.randomUUID(), "PROFILE_READ", "Consulter son profil")));
        when(rolePermissionService.resolveRoles(Set.of("PATIENT"))).thenReturn(Set.of(patientRole));
        when(rolePermissionService.toResponse(patientRole))
                .thenReturn(new RoleResponse("PATIENT", "Patient", Set.of()));
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountApplicationService.createAccount(new CreateAccountRequest(
                "alice",
                "alice@hopital.local",
                "Alice",
                "plain-password",
                UUID.randomUUID().toString(),
                "cHJvZmlsZS1waG90bw==",
                "image/jpeg",
                Set.of("PATIENT")));

        verify(accountRepository).save(accountCaptor.capture());
        verify(applicationEventPublisher).publishEvent(accountCreatedEventCaptor.capture());
        assertThat(accountCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(accountCaptor.getValue().getProfilePhotoBase64()).isEqualTo("cHJvZmlsZS1waG90bw==");
        assertThat(accountCaptor.getValue().getProfilePhotoContentType()).isEqualTo("image/jpeg");
        assertThat(response.username()).isEqualTo("alice");
        assertThat(accountCreatedEventCaptor.getValue().account().email()).isEqualTo("alice@hopital.local");
        assertThat(response.roles()).singleElement().satisfies(role -> assertThat(role.code()).isEqualTo("PATIENT"));
    }

    @Test
    void validatesCredentialsAgainstThePasswordHash() {
        AccountEntity account = new AccountEntity(
                UUID.randomUUID(),
                "alice",
                "alice@hopital.local",
                "Alice",
                "hashed-password",
                UUID.randomUUID(),
                null,
                null,
                Set.of());
        when(accountRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("alice", "alice"))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);

        var response = accountApplicationService.validateCredentials(
                new CredentialsValidationRequest("alice", "plain-password"));

        verify(passwordEncoder).matches(eq("plain-password"), eq("hashed-password"));
        assertThat(response.authenticated()).isTrue();
        assertThat(response.account().username()).isEqualTo("alice");
    }

    @Test
    void refusesSignInForANonAdministratorWithoutAHospitalAssignment() {
        AccountEntity account = new AccountEntity(
                UUID.randomUUID(),
                "alice",
                "alice@hopital.local",
                "Alice",
                "hashed-password",
                null,
                null,
                null,
                Set.of());
        when(accountRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("alice", "alice"))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);

        var response = accountApplicationService.validateCredentials(
                new CredentialsValidationRequest("alice", "plain-password"));

        assertThat(response.authenticated()).isFalse();
        assertThat(response.account()).isNull();
    }

    @Test
    void allowsAnAdministratorToSignInWithoutAHospitalAssignment() {
        RoleEntity administratorRole = new RoleEntity(UUID.randomUUID(), "ADMIN", "Administrateur", Set.of());
        AccountEntity account = new AccountEntity(
                UUID.randomUUID(),
                "admin",
                "admin@hopital.local",
                "Administrateur",
                "hashed-password",
                null,
                null,
                null,
                Set.of(administratorRole));
        when(accountRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("admin", "admin"))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);
        when(rolePermissionService.toResponse(administratorRole))
                .thenReturn(new RoleResponse("ADMIN", "Administrateur", Set.of()));

        var response = accountApplicationService.validateCredentials(
                new CredentialsValidationRequest("admin", "plain-password"));

        assertThat(response.authenticated()).isTrue();
        assertThat(response.account().hospitalId()).isNull();
    }
}
