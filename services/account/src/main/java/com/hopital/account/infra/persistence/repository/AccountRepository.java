package com.hopital.account.infra.persistence.repository;

import com.hopital.account.infra.persistence.entity.AccountEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    Optional<AccountEntity> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    List<AccountEntity> findAllByOrderByDisplayNameAsc();

    @Query("""
            SELECT account
            FROM AccountEntity account
            WHERE (:query = ''
                    OR LOWER(account.username) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(account.email) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(account.displayName) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:hospitalId IS NULL OR account.hospitalId = :hospitalId)
            """)
    Page<AccountEntity> search(
            @Param("query") String query,
            @Param("hospitalId") UUID hospitalId,
            Pageable pageable);
}
