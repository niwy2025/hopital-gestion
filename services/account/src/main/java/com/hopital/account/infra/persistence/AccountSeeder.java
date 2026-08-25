package com.hopital.account.infra.persistence;

import com.hopital.account.application.config.AccountSeedProperties;
import com.hopital.account.application.service.RolePermissionService;
import com.hopital.account.infra.persistence.entity.AccountEntity;
import com.hopital.account.infra.persistence.repository.AccountRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AccountSeeder implements ApplicationRunner {

    private final AccountRepository accountRepository;
    private final RolePermissionService rolePermissionService;
    private final AccountSeedProperties seed;
    private final PasswordEncoder passwordEncoder;

    public AccountSeeder(
            AccountRepository accountRepository,
            RolePermissionService rolePermissionService,
            AccountSeedProperties seed,
            PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.rolePermissionService = rolePermissionService;
        this.seed = seed;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (accountRepository.existsByUsernameIgnoreCaseOrEmailIgnoreCase(seed.adminUsername(), seed.adminEmail())) {
            return;
        }
        accountRepository.save(new AccountEntity(
                UUID.randomUUID(),
                seed.adminUsername(),
                seed.adminEmail(),
                "Administrateur Hopital",
                passwordEncoder.encode(seed.adminPassword()),
                null,
                null,
                null,
                rolePermissionService.resolveRoles(Set.of("ADMIN"))));
    }
}
