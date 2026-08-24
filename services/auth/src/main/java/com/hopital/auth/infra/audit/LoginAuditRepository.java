package com.hopital.auth.infra.audit;

import com.hopital.auth.application.dto.AccountResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoginAuditRepository {

    private final JdbcTemplate jdbcTemplate;

    public LoginAuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void recordSuccess(AccountResponse account, String userAgent, Instant accessTokenExpiresAt) {
        record(account.id(), account.username(), userAgent, "SUCCESS", accessTokenExpiresAt);
    }

    public void recordFailure(String username, String userAgent) {
        record(null, username, userAgent, "FAILURE", null);
    }

    private void record(String accountId, String username, String userAgent, String status, Instant accessTokenExpiresAt) {
        jdbcTemplate.update(
                """
                INSERT INTO auth_login_audits (account_id, username, user_agent, status, access_token_expires_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                accountId == null ? null : UUID.fromString(accountId),
                username == null || username.isBlank() ? "unknown" : username,
                userAgent == null || userAgent.isBlank() ? "unknown" : userAgent.substring(0, Math.min(userAgent.length(), 1024)),
                status,
                accessTokenExpiresAt == null ? null : Timestamp.from(accessTokenExpiresAt));
    }
}
