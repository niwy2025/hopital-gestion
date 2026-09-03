package com.hopital.accounting.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Background worker only; a user action never waits on patient-service. */
@Component
public class PharmacyPaymentSettlementOutboxRetryScheduler {

    private final PharmacyPaymentSettlementOutboxDispatcher dispatcher;

    public PharmacyPaymentSettlementOutboxRetryScheduler(PharmacyPaymentSettlementOutboxDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${hospital.patient-service.payment-settlement-outbox.retry-delay-ms:30000}")
    public void retryDueEvents() {
        dispatcher.pendingEventIds().forEach(dispatcher::deliverDue);
    }
}
