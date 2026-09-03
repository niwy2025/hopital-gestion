package com.hopital.accounting.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingSourceType;
import com.hopital.accounting.application.domain.InvoiceStatus;
import com.hopital.accounting.application.domain.PharmacyPaymentSettlementEventType;
import com.hopital.accounting.application.domain.PharmacyPaymentSettlementOutboxStatus;
import com.hopital.accounting.infra.integration.patient.PatientPharmacyPaymentSettlementClient;
import com.hopital.accounting.infra.persistence.entity.AccountingInvoiceEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingPaymentEntity;
import com.hopital.accounting.infra.persistence.entity.PharmacyPaymentSettlementOutboxEventEntity;
import com.hopital.accounting.infra.persistence.repository.PharmacyPaymentSettlementOutboxEventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PharmacyPaymentSettlementOutboxTest {

    @Mock private PharmacyPaymentSettlementOutboxEventRepository eventRepository;
    @Mock private PatientPharmacyPaymentSettlementClient patientClient;

    @Test
    void snapshotsIssuedPharmacyInvoiceAsAnInitialProjection() {
        AccountingInvoiceEntity invoice = pharmacyInvoice(1, new BigDecimal("0.00"), new BigDecimal("100.00"), InvoiceStatus.ISSUED);
        when(eventRepository.existsByInvoiceIdAndStateVersion(invoice.getId(), 1)).thenReturn(false);
        PharmacyPaymentSettlementOutboxService service = new PharmacyPaymentSettlementOutboxService(eventRepository);

        service.enqueueInvoiceState(invoice);

        ArgumentCaptor<PharmacyPaymentSettlementOutboxEventEntity> captor = ArgumentCaptor.forClass(
                PharmacyPaymentSettlementOutboxEventEntity.class);
        verify(eventRepository).save(captor.capture());
        PharmacyPaymentSettlementOutboxEventEntity event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo(PharmacyPaymentSettlementEventType.INVOICE_ISSUED);
        assertThat(event.getPaymentId()).isNull();
        assertThat(event.getInvoiceCode()).isEqualTo("FAC-001");
        assertThat(event.getDispenseCode()).isEqualTo("DSP-001");
        assertThat(event.getTotalAmount()).isEqualByComparingTo("100.00");
        assertThat(event.getPaidAmount()).isEqualByComparingTo("0.00");
        assertThat(event.getDueAmount()).isEqualByComparingTo("100.00");
        assertThat(event.getStateVersion()).isEqualTo(1);
    }

    @Test
    void snapshotsEveryPharmacyPaymentWithItsOwnIdempotencyKey() {
        AccountingInvoiceEntity invoice = pharmacyInvoice(2, new BigDecimal("40.00"), new BigDecimal("60.00"), InvoiceStatus.PARTIALLY_PAID);
        AccountingPaymentEntity payment = mock(AccountingPaymentEntity.class);
        UUID paymentId = UUID.randomUUID();
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getPaidOn()).thenReturn(LocalDate.of(2026, 9, 3));
        when(payment.getPaymentReference()).thenReturn("CAISSE-42");
        when(eventRepository.existsByPaymentId(paymentId)).thenReturn(false);
        PharmacyPaymentSettlementOutboxService service = new PharmacyPaymentSettlementOutboxService(eventRepository);

        service.enqueuePaymentState(payment, invoice);

        ArgumentCaptor<PharmacyPaymentSettlementOutboxEventEntity> captor = ArgumentCaptor.forClass(
                PharmacyPaymentSettlementOutboxEventEntity.class);
        verify(eventRepository).save(captor.capture());
        PharmacyPaymentSettlementOutboxEventEntity event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo(PharmacyPaymentSettlementEventType.PAYMENT_RECORDED);
        assertThat(event.getEventKey()).isEqualTo("PAYMENT:" + paymentId);
        assertThat(event.getPaymentId()).isEqualTo(paymentId);
        assertThat(event.getPaidOn()).isEqualTo(LocalDate.of(2026, 9, 3));
        assertThat(event.getPaymentReference()).isEqualTo("CAISSE-42");
        assertThat(event.getStateVersion()).isEqualTo(2);
    }

    @Test
    void neverProjectsARegularManualInvoice() {
        AccountingInvoiceEntity invoice = mock(AccountingInvoiceEntity.class);
        when(invoice.getSourceType()).thenReturn(AccountingSourceType.MANUAL_INVOICE);
        PharmacyPaymentSettlementOutboxService service = new PharmacyPaymentSettlementOutboxService(eventRepository);

        service.enqueueInvoiceState(invoice);

        verifyNoInteractions(eventRepository);
    }

    @Test
    void marksTheEventPostedOnlyAfterPatientAcceptsIt() {
        PharmacyPaymentSettlementOutboxEventEntity event = initialEvent();
        when(eventRepository.lockById(event.getId())).thenReturn(Optional.of(event));
        PharmacyPaymentSettlementOutboxDispatcher dispatcher = new PharmacyPaymentSettlementOutboxDispatcher(eventRepository, patientClient);

        dispatcher.deliverDue(event.getId());

        verify(patientClient).postSettlement(event);
        assertThat(event.getStatus()).isEqualTo(PharmacyPaymentSettlementOutboxStatus.POSTED);
        assertThat(event.getProcessedAt()).isNotNull();
        assertThat(event.getLastError()).isNull();
    }

    @Test
    void schedulesAControlledRetryWhenPatientServiceIsUnavailable() {
        PharmacyPaymentSettlementOutboxEventEntity event = initialEvent();
        when(eventRepository.lockById(event.getId())).thenReturn(Optional.of(event));
        org.mockito.Mockito.doThrow(new IllegalStateException("patient unavailable"))
                .when(patientClient).postSettlement(any());
        PharmacyPaymentSettlementOutboxDispatcher dispatcher = new PharmacyPaymentSettlementOutboxDispatcher(eventRepository, patientClient);
        Instant before = Instant.now();

        dispatcher.deliverDue(event.getId());

        assertThat(event.getStatus()).isEqualTo(PharmacyPaymentSettlementOutboxStatus.PENDING);
        assertThat(event.getAttemptCount()).isEqualTo(1);
        assertThat(event.getNextAttemptAt()).isAfter(before);
        assertThat(event.getLastError()).contains("patient unavailable");
    }

    private PharmacyPaymentSettlementOutboxEventEntity initialEvent() {
        return PharmacyPaymentSettlementOutboxEventEntity.invoiceIssued(
                pharmacyInvoice(1, BigDecimal.ZERO.setScale(2), new BigDecimal("100.00"), InvoiceStatus.ISSUED),
                Instant.now().minusSeconds(1));
    }

    private AccountingInvoiceEntity pharmacyInvoice(
            int stateVersion,
            BigDecimal paidAmount,
            BigDecimal dueAmount,
            InvoiceStatus status) {
        AccountingInvoiceEntity invoice = new AccountingInvoiceEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HOP-001",
                "FAC-001",
                AccountingSourceType.PHARMACY_DISPENSE,
                "DSP-001",
                UUID.randomUUID(),
                "PAT-001",
                UUID.randomUUID(),
                "PAS-001",
                LocalDate.of(2026, 9, 3),
                AccountingCurrency.CDF,
                paidAmount.add(dueAmount),
                "Délivrance pharmacie DSP-001",
                "system",
                "system",
                Instant.now());
        invoice.issue();
        if (paidAmount.signum() > 0) {
            invoice.receive(paidAmount);
        }
        assertThat(invoice.getSettlementVersion()).isEqualTo(stateVersion);
        assertThat(invoice.getStatus()).isEqualTo(status);
        return invoice;
    }
}
