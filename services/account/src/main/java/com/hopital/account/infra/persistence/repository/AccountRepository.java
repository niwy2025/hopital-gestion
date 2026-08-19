package com.hopital.account.infra.persistence.repository;

import com.hopital.account.infra.persistence.entity.AccountEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    boolean existsByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);
}
