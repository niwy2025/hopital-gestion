package com.hopital.accounting.application.dto;

import com.hopital.accounting.application.domain.JournalType;
import java.time.Instant;
import java.util.UUID;

public record AccountingJournalResponse(UUID id, UUID hospitalId, String hospitalCode, String code, String label,
        JournalType type, boolean active, boolean systemJournal, Instant createdAt) { }
