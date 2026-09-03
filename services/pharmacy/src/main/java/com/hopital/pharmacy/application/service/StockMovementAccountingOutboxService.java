package com.hopital.pharmacy.application.service;

import com.hopital.pharmacy.application.domain.StockMovementSourceType;
import com.hopital.pharmacy.application.domain.StockMovementType;
import com.hopital.pharmacy.infra.persistence.entity.StockMovementAccountingOutboxEventEntity;
import com.hopital.pharmacy.infra.persistence.entity.StockMovementEntity;
import com.hopital.pharmacy.infra.persistence.repository.StockMovementAccountingOutboxEventRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Queues only stock-out movements that require their own accounting voucher. */
@Service
public class StockMovementAccountingOutboxService {

    private final StockMovementAccountingOutboxEventRepository outboxEventRepository;

    public StockMovementAccountingOutboxService(StockMovementAccountingOutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    public void enqueueIfRequired(StockMovementEntity movement) {
        if (!requiresAccounting(movement)
                || movement.getSourceType() == StockMovementSourceType.PRESCRIPTION_DISPENSE
                || isLegacyPrescriptionDispense(movement)
                || outboxEventRepository.findByStockMovementId(movement.getId()).isPresent()) {
            return;
        }
        outboxEventRepository.save(new StockMovementAccountingOutboxEventEntity(
                UUID.randomUUID(), movement.getId(), movement.getCode(), Instant.now()));
    }

    private boolean requiresAccounting(StockMovementEntity movement) {
        return switch (movement.getType()) {
            case LOSS, EXPIRY, TRANSFER_OUT, DISPENSING -> true;
            case ENTRY -> false;
        };
    }

    /** Old rows may have a DSP reference but predate source_type. */
    private boolean isLegacyPrescriptionDispense(StockMovementEntity movement) {
        return movement.getType() == StockMovementType.DISPENSING
                && movement.getSourceCode() != null
                && movement.getSourceCode().trim().toUpperCase(Locale.ROOT).startsWith("DSP-");
    }
}
