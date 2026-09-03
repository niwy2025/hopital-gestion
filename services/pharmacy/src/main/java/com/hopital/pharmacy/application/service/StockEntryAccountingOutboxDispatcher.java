package com.hopital.pharmacy.application.service;

import com.hopital.pharmacy.application.domain.StockEntryAccountingOutboxStatus;
import com.hopital.pharmacy.infra.integration.accounting.AccountingPharmacyStockEntryClient;
import com.hopital.pharmacy.infra.persistence.entity.StockEntryAccountingOutboxEventEntity;
import com.hopital.pharmacy.infra.persistence.entity.StockEntryEntity;
import com.hopital.pharmacy.infra.persistence.repository.StockEntryAccountingOutboxEventRepository;
import com.hopital.pharmacy.infra.persistence.repository.StockEntryRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Background delivery of stock-receipt accounting events with bounded retry. */
@Service
public class StockEntryAccountingOutboxDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockEntryAccountingOutboxDispatcher.class);
    private static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(15);
    private static final Duration MAX_RETRY_DELAY = Duration.ofMinutes(30);

    private final StockEntryAccountingOutboxEventRepository outboxEventRepository;
    private final StockEntryRepository stockEntryRepository;
    private final AccountingPharmacyStockEntryClient accountingClient;

    public StockEntryAccountingOutboxDispatcher(
            StockEntryAccountingOutboxEventRepository outboxEventRepository,
            StockEntryRepository stockEntryRepository,
            AccountingPharmacyStockEntryClient accountingClient) {
        this.outboxEventRepository = outboxEventRepository;
        this.stockEntryRepository = stockEntryRepository;
        this.accountingClient = accountingClient;
    }

    public List<String> pendingStockEntryCodes() {
        return outboxEventRepository
                .findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        StockEntryAccountingOutboxStatus.PENDING, Instant.now())
                .stream()
                .map(StockEntryAccountingOutboxEventEntity::getStockEntryCode)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliverDue(String stockEntryCode) {
        StockEntryAccountingOutboxEventEntity event = outboxEventRepository.lockByStockEntryCode(stockEntryCode).orElse(null);
        if (event == null || event.getStatus() == StockEntryAccountingOutboxStatus.POSTED) {
            return;
        }
        Instant now = Instant.now();
        if (event.getNextAttemptAt().isAfter(now)) {
            return;
        }
        try {
            String entryReference = accountingClient.postStockEntry(event.getStockEntryCode());
            StockEntryEntity stockEntry = stockEntryRepository.findById(event.getStockEntryId()).orElse(null);
            if (stockEntry == null) {
                throw new IllegalStateException("L'entrée de stock source n'existe plus.");
            }
            stockEntry.markAccountingPosted(entryReference);
            event.markPosted(entryReference, now);
        } catch (RuntimeException exception) {
            event.scheduleRetry(nextRetryAt(event.getAttemptCount(), now), errorMessage(exception), now);
            LOGGER.warn(
                    "La synchronisation comptable de l'entrée de stock {} sera réessayée (tentative {}).",
                    event.getStockEntryCode(),
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
