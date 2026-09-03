package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.AccountingPeriodStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AccountingPeriodResponse(UUID id, UUID hospitalId, String hospitalCode, String code, String label,
        LocalDate startsOn, LocalDate endsOn, AccountingPeriodStatus status, Instant closedAt, String closedByUsername,
        Instant createdAt) { }
