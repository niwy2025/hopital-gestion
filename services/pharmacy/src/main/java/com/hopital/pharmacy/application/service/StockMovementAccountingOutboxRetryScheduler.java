package com.hopital.pharmacy.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Stock-out recording never blocks on the accounting service. */
@Component
public class StockMovementAccountingOutboxRetryScheduler {

    private final StockMovementAccountingOutboxDispatcher dispatcher;

    public StockMovementAccountingOutboxRetryScheduler(StockMovementAccountingOutboxDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${hospital.accounting-service.outbox.retry-delay-ms:30000}")
    public void retryDueEvents() {
        dispatcher.pendingStockMovementCodes().forEach(dispatcher::deliverDue);
    }
}
