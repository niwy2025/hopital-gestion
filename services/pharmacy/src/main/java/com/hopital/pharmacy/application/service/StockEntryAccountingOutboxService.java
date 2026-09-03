package com.hopital.pharmacy.application.service;

import com.hopital.pharmacy.infra.persistence.entity.StockEntryAccountingOutboxEventEntity;
import com.hopital.pharmacy.infra.persistence.repository.StockEntryAccountingOutboxEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Queues accounting work in the same transaction as a stock reception. */
@Service
public class StockEntryAccountingOutboxService {

    private final StockEntryAccountingOutboxEventRepository outboxEventRepository;

    public StockEntryAccountingOutboxService(StockEntryAccountingOutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    public void enqueue(UUID stockEntryId, String stockEntryCode) {
        if (outboxEventRepository.findByStockEntryId(stockEntryId).isPresent()) {
            return;
        }
        outboxEventRepository.save(new StockEntryAccountingOutboxEventEntity(
                UUID.randomUUID(), stockEntryId, stockEntryCode, Instant.now()));
    }
}
