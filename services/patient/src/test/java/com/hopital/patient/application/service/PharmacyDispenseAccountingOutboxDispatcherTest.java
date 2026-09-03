package com.hopital.patient.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import com.hopital.patient.application.domain.AccountingOutboxStatus;
import com.hopital.patient.infra.integration.accounting.AccountingPharmacyDispenseClient;
import com.hopital.patient.infra.persistence.entity.PharmacyDispenseAccountingOutboxEventEntity;
import com.hopital.patient.infra.persistence.repository.PharmacyDispenseAccountingOutboxEventRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PharmacyDispenseAccountingOutboxDispatcherTest {

    @Mock
    private PharmacyDispenseAccountingOutboxEventRepository outboxEventRepository;

    @Mock
    private AccountingPharmacyDispenseClient accountingPharmacyDispenseClient;

    @InjectMocks
    private PharmacyDispenseAccountingOutboxDispatcher dispatcher;

    @Test
    void postsOnePendingDeliveryAndKeepsTheAccountingInvoiceReference() {
        PharmacyDispenseAccountingOutboxEventEntity event = pendingEvent();
        when(outboxEventRepository.lockByDispenseCode(event.getDispenseCode())).thenReturn(Optional.of(event));
        when(accountingPharmacyDispenseClient.postDispense(event.getDispenseCode())).thenReturn("FAC-20260903-0001");

        dispatcher.deliver(event.getDispenseCode());

        assertThat(event.getStatus()).isEqualTo(AccountingOutboxStatus.POSTED);
        assertThat(event.getInvoiceReference()).isEqualTo("FAC-20260903-0001");
        assertThat(event.getProcessedAt()).isNotNull();
        assertThat(event.getLastError()).isNull();
    }

    @Test
    void retainsTheDeliveryForRetryWhenAccountingIsTemporarilyUnavailable() {
        PharmacyDispenseAccountingOutboxEventEntity event = pendingEvent();
        Instant dueAt = event.getNextAttemptAt();
        when(outboxEventRepository.lockByDispenseCode(event.getDispenseCode())).thenReturn(Optional.of(event));
        when(accountingPharmacyDispenseClient.postDispense(event.getDispenseCode()))
                .thenThrow(new IllegalStateException("Service Comptabilité indisponible"));

        dispatcher.deliver(event.getDispenseCode());

        assertThat(event.getStatus()).isEqualTo(AccountingOutboxStatus.PENDING);
        assertThat(event.getAttemptCount()).isEqualTo(1);
        assertThat(event.getNextAttemptAt()).isAfter(dueAt);
        assertThat(event.getLastError()).isEqualTo("Service Comptabilité indisponible");
    }

    @Test
    void doesNotPostAnAlreadyAcknowledgedDeliveryTwice() {
        PharmacyDispenseAccountingOutboxEventEntity event = pendingEvent();
        event.markPosted("FAC-20260903-0001", Instant.now());
        when(outboxEventRepository.lockByDispenseCode(event.getDispenseCode())).thenReturn(Optional.of(event));

        dispatcher.deliver(event.getDispenseCode());

        assertThat(event.getStatus()).isEqualTo(AccountingOutboxStatus.POSTED);
        assertThat(event.getAttemptCount()).isZero();
    }

    @Test
    void honoursTheRetryWindowWhenAnotherNodeHasAlreadyRescheduledTheEvent() {
        PharmacyDispenseAccountingOutboxEventEntity event = pendingEvent();
        event.scheduleRetry(Instant.now().plusSeconds(30), "Indisponible", Instant.now());
        when(outboxEventRepository.lockByDispenseCode(event.getDispenseCode())).thenReturn(Optional.of(event));

        dispatcher.deliverDue(event.getDispenseCode());

        verifyNoInteractions(accountingPharmacyDispenseClient);
        assertThat(event.getAttemptCount()).isEqualTo(1);
    }

    private PharmacyDispenseAccountingOutboxEventEntity pendingEvent() {
        return new PharmacyDispenseAccountingOutboxEventEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "DSP-20260903-ABCDEFGH",
                Instant.now());
    }
}
