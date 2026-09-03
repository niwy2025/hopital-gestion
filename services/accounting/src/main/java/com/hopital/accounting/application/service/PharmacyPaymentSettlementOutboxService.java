package com.hopital.accounting.application.service;

import com.hopital.accounting.application.domain.AccountingSourceType;
import com.hopital.accounting.infra.persistence.entity.AccountingInvoiceEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingPaymentEntity;
import com.hopital.accounting.infra.persistence.entity.PharmacyPaymentSettlementOutboxEventEntity;
import com.hopital.accounting.infra.persistence.repository.PharmacyPaymentSettlementOutboxEventRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

/**
 * Persists pharmacy payment projections in the accounting transaction. No
 * network call occurs here: the dispatcher delivers after the commit and can
 * safely retry when patient-service is unavailable.
 */
@Service
public class PharmacyPaymentSettlementOutboxService {

    private final PharmacyPaymentSettlementOutboxEventRepository eventRepository;

    public PharmacyPaymentSettlementOutboxService(
            PharmacyPaymentSettlementOutboxEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void enqueueInvoiceState(AccountingInvoiceEntity invoice) {
        if (invoice.getSourceType() != AccountingSourceType.PHARMACY_DISPENSE) {
            return;
        }
        // A later payment event carries the same state version. Re-enqueuing
        // an invoice retry must not create a redundant projection in that
        // case, but must recreate an absent initial/current snapshot.
        if (eventRepository.existsByInvoiceIdAndStateVersion(invoice.getId(), invoice.getSettlementVersion())) {
            return;
        }
        eventRepository.save(PharmacyPaymentSettlementOutboxEventEntity.invoiceIssued(invoice, Instant.now()));
    }

    public void enqueuePaymentState(AccountingPaymentEntity payment, AccountingInvoiceEntity invoice) {
        if (invoice.getSourceType() != AccountingSourceType.PHARMACY_DISPENSE) {
            return;
        }
        if (eventRepository.existsByPaymentId(payment.getId())) {
            return;
        }
        eventRepository.save(PharmacyPaymentSettlementOutboxEventEntity.paymentRecorded(payment, invoice, Instant.now()));
    }
}
