package com.hopital.accounting.application.service;

import com.hopital.accounting.application.domain.PharmacyPaymentSettlementOutboxStatus;
import com.hopital.accounting.infra.integration.patient.PatientPharmacyPaymentSettlementClient;
import com.hopital.accounting.infra.persistence.entity.PharmacyPaymentSettlementOutboxEventEntity;
import com.hopital.accounting.infra.persistence.repository.PharmacyPaymentSettlementOutboxEventRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * At-least-once delivery of accounting settlement states. The receiver must
 * deduplicate on eventId; marking an event posted only happens after a 2xx.
 */
@Service
public class PharmacyPaymentSettlementOutboxDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PharmacyPaymentSettlementOutboxDispatcher.class);
    private static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(15);
    private static final Duration MAX_RETRY_DELAY = Duration.ofMinutes(30);

    private final PharmacyPaymentSettlementOutboxEventRepository eventRepository;
    private final PatientPharmacyPaymentSettlementClient patientClient;

    public PharmacyPaymentSettlementOutboxDispatcher(
            PharmacyPaymentSettlementOutboxEventRepository eventRepository,
            PatientPharmacyPaymentSettlementClient patientClient) {
        this.eventRepository = eventRepository;
        this.patientClient = patientClient;
    }

    public List<UUID> pendingEventIds() {
        return eventRepository.findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        PharmacyPaymentSettlementOutboxStatus.PENDING, Instant.now())
                .stream()
                .map(PharmacyPaymentSettlementOutboxEventEntity::getId)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliverDue(UUID eventId) {
        PharmacyPaymentSettlementOutboxEventEntity event = eventRepository.lockById(eventId).orElse(null);
        if (event == null || event.getStatus() == PharmacyPaymentSettlementOutboxStatus.POSTED) {
            return;
        }
        Instant now = Instant.now();
        if (event.getNextAttemptAt().isAfter(now)) {
            return;
        }
        try {
            patientClient.postSettlement(event);
            event.markPosted(now);
        } catch (RuntimeException exception) {
            event.scheduleRetry(nextRetryAt(event.getAttemptCount(), now), errorMessage(exception), now);
            LOGGER.warn(
                    "La projection du règlement pharmacie {} vers le dossier patient sera réessayée (tentative {}).",
                    event.getInvoiceCode(),
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
