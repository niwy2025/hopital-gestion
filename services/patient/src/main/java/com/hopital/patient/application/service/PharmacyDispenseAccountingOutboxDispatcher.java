package com.hopital.patient.application.service;

import com.hopital.patient.application.domain.AccountingOutboxStatus;
import com.hopital.patient.infra.integration.accounting.AccountingPharmacyDispenseClient;
import com.hopital.patient.infra.persistence.entity.PharmacyDispenseAccountingOutboxEventEntity;
import com.hopital.patient.infra.persistence.repository.PharmacyDispenseAccountingOutboxEventRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Delivers outbox messages after commit and periodically retries unavailable accounting. */
@Service
public class PharmacyDispenseAccountingOutboxDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PharmacyDispenseAccountingOutboxDispatcher.class);
    private static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(15);
    private static final Duration MAX_RETRY_DELAY = Duration.ofMinutes(30);

    private final PharmacyDispenseAccountingOutboxEventRepository outboxEventRepository;
    private final AccountingPharmacyDispenseClient accountingPharmacyDispenseClient;

    public PharmacyDispenseAccountingOutboxDispatcher(
            PharmacyDispenseAccountingOutboxEventRepository outboxEventRepository,
            AccountingPharmacyDispenseClient accountingPharmacyDispenseClient) {
        this.outboxEventRepository = outboxEventRepository;
        this.accountingPharmacyDispenseClient = accountingPharmacyDispenseClient;
    }

    public List<String> pendingDispenseCodes() {
        return outboxEventRepository
                .findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        AccountingOutboxStatus.PENDING, Instant.now())
                .stream()
                .map(PharmacyDispenseAccountingOutboxEventEntity::getDispenseCode)
                .toList();
    }

    /**
     * The pessimistic lock serializes an immediate after-commit delivery with a
     * scheduler delivery. The accounting endpoint must still be idempotent for
     * the small unavoidable window after its own successful commit.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliver(String dispenseCode) {
        deliver(dispenseCode, true);
    }

    /** Delivers only an event whose backoff window has elapsed. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliverDue(String dispenseCode) {
        deliver(dispenseCode, false);
    }

    private void deliver(String dispenseCode, boolean ignoreBackoff) {
        PharmacyDispenseAccountingOutboxEventEntity event = outboxEventRepository.lockByDispenseCode(dispenseCode)
                .orElse(null);
        if (event == null || event.getStatus() == AccountingOutboxStatus.POSTED) {
            return;
        }

        Instant now = Instant.now();
        if (!ignoreBackoff && event.getNextAttemptAt().isAfter(now)) {
            return;
        }
        try {
            String invoiceReference = accountingPharmacyDispenseClient.postDispense(event.getDispenseCode());
            event.markPosted(invoiceReference, now);
        } catch (RuntimeException exception) {
            event.scheduleRetry(nextRetryAt(event.getAttemptCount(), now), errorMessage(exception), now);
            LOGGER.warn(
                    "La synchronisation comptable de la délivrance {} sera réessayée (tentative {}).",
                    event.getDispenseCode(),
                    event.getAttemptCount());
        }
    }

    private Instant nextRetryAt(int currentAttemptCount, Instant now) {
        int exponent = Math.min(Math.max(currentAttemptCount, 0), 10);
        long multiplier = 1L << exponent;
        Duration candidate = INITIAL_RETRY_DELAY.multipliedBy(multiplier);
        return now.plus(candidate.compareTo(MAX_RETRY_DELAY) > 0 ? MAX_RETRY_DELAY : candidate);
    }

    private String errorMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.length() <= 1_500 ? message : message.substring(0, 1_500);
    }
}
