package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountNature;
import java.time.Instant;
import java.util.UUID;

public record AccountingAccountResponse(UUID id, UUID hospitalId, String hospitalCode, String accountNumber, String label,
        String accountClass, AccountNature nature, boolean active, boolean systemAccount, Instant createdAt) { }
