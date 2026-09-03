package com.hopital.patient.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Background retry only; no browser or public endpoint can trigger it. */
@Component
public class PharmacyDispenseAccountingOutboxRetryScheduler {

    private final PharmacyDispenseAccountingOutboxDispatcher dispatcher;

    public PharmacyDispenseAccountingOutboxRetryScheduler(PharmacyDispenseAccountingOutboxDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${hospital.accounting-service.outbox.retry-delay-ms:30000}")
    public void retryDueEvents() {
        dispatcher.pendingDispenseCodes().forEach(dispatcher::deliverDue);
    }
}
