package com.hopital.pharmacy.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Background-only retry. Stock reception endpoints never wait on accounting. */
@Component
public class StockEntryAccountingOutboxRetryScheduler {

    private final StockEntryAccountingOutboxDispatcher dispatcher;

    public StockEntryAccountingOutboxRetryScheduler(StockEntryAccountingOutboxDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${hospital.accounting-service.outbox.retry-delay-ms:30000}")
    public void retryDueEvents() {
        dispatcher.pendingStockEntryCodes().forEach(dispatcher::deliverDue);
    }
}
