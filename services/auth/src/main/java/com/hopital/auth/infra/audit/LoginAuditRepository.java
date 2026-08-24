package com.hopital.auth.infra.audit;

import com.hopital.auth.application.dto.AccountResponse;
import com.hopital.auth.application.dto.KnownDeviceResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
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

    public List<KnownDeviceResponse> findKnownDevices(String accountId, String currentUserAgent) {
        return jdbcTemplate.query(
                """
                SELECT user_agent, MAX(occurred_at) AS last_seen_at, COUNT(*) AS sign_in_count
                FROM auth_login_audits
                WHERE account_id = ? AND status = 'SUCCESS'
                GROUP BY user_agent
                ORDER BY MAX(occurred_at) DESC
                """,
                (resultSet, rowNumber) -> new KnownDeviceResponse(
                        resultSet.getString("user_agent"),
                        resultSet.getTimestamp("last_seen_at").toInstant(),
                        resultSet.getLong("sign_in_count"),
                        resultSet.getString("user_agent").equals(currentUserAgent)),
                UUID.fromString(accountId));
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
