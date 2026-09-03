package com.hopital.patient.application.service;

import com.hopital.patient.infra.persistence.entity.PharmacyDispenseAccountingOutboxEventEntity;
import com.hopital.patient.infra.persistence.repository.PharmacyDispenseAccountingOutboxEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Queues accounting work in the same transaction as a successful dispense.
 *
 * <p>The scheduler owns delivery. In particular, this service deliberately
 * does not make an HTTP call after commit: a slow or unavailable accounting
 * service must never hold up the pharmacy user's response.</p>
 */
@Service
public class PharmacyDispenseAccountingOutboxService {

    private final PharmacyDispenseAccountingOutboxEventRepository outboxEventRepository;

    public PharmacyDispenseAccountingOutboxService(
            PharmacyDispenseAccountingOutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    public void enqueue(UUID dispenseId, String dispenseCode) {
        if (outboxEventRepository.findByDispenseId(dispenseId).isPresent()) {
            return;
        }
        outboxEventRepository.save(new PharmacyDispenseAccountingOutboxEventEntity(
                UUID.randomUUID(),
                dispenseId,
                dispenseCode,
                Instant.now()));
    }
}
