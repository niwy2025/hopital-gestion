package com.hopital.pharmacy.application.service;

import com.hopital.pharmacy.application.domain.StockMovementAccountingOutboxStatus;
import com.hopital.pharmacy.infra.integration.accounting.AccountingPharmacyStockMovementClient;
import com.hopital.pharmacy.infra.persistence.entity.StockMovementAccountingOutboxEventEntity;
import com.hopital.pharmacy.infra.persistence.repository.StockMovementAccountingOutboxEventRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Background delivery of pharmacy stock-out accounting events with bounded retry. */
@Service
public class StockMovementAccountingOutboxDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockMovementAccountingOutboxDispatcher.class);
    private static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(15);
    private static final Duration MAX_RETRY_DELAY = Duration.ofMinutes(30);

    private final StockMovementAccountingOutboxEventRepository outboxEventRepository;
    private final AccountingPharmacyStockMovementClient accountingClient;

    public StockMovementAccountingOutboxDispatcher(
            StockMovementAccountingOutboxEventRepository outboxEventRepository,
            AccountingPharmacyStockMovementClient accountingClient) {
        this.outboxEventRepository = outboxEventRepository;
        this.accountingClient = accountingClient;
    }

    public List<String> pendingStockMovementCodes() {
        return outboxEventRepository
                .findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        StockMovementAccountingOutboxStatus.PENDING, Instant.now())
                .stream()
                .map(StockMovementAccountingOutboxEventEntity::getStockMovementCode)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliverDue(String stockMovementCode) {
        StockMovementAccountingOutboxEventEntity event = outboxEventRepository
                .lockByStockMovementCode(stockMovementCode)
                .orElse(null);
        if (event == null || event.getStatus() != StockMovementAccountingOutboxStatus.PENDING) {
            return;
        }
        Instant now = Instant.now();
        if (event.getNextAttemptAt().isAfter(now)) {
            return;
        }
        try {
            AccountingPharmacyStockMovementClient.AccountingPostingAcknowledgement acknowledgement = accountingClient
                    .postStockMovement(event.getStockMovementCode());
            if (acknowledgement.ignored()) {
                event.markExcluded(defaultIfBlank(acknowledgement.ignoredReason(),
                        "Mouvement déjà traité par une autre filière comptable."), now);
                return;
            }
            event.markPosted(acknowledgement.accountingEntryReference().trim(), now);
        } catch (RuntimeException exception) {
            event.scheduleRetry(nextRetryAt(event.getAttemptCount(), now), errorMessage(exception), now);
            LOGGER.warn(
                    "La synchronisation comptable de la sortie de stock {} sera réessayée (tentative {}).",
                    event.getStockMovementCode(),
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

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
