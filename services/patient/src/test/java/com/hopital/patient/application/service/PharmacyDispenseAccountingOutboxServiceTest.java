package com.hopital.patient.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hopital.patient.infra.persistence.entity.PharmacyDispenseAccountingOutboxEventEntity;
import com.hopital.patient.infra.persistence.repository.PharmacyDispenseAccountingOutboxEventRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PharmacyDispenseAccountingOutboxServiceTest {

    @Mock
    private PharmacyDispenseAccountingOutboxEventRepository outboxEventRepository;

    @InjectMocks
    private PharmacyDispenseAccountingOutboxService outboxService;

    @Test
    void persistsAPendingEventForTheBackgroundScheduler() {
        UUID dispenseId = UUID.randomUUID();
        when(outboxEventRepository.findByDispenseId(dispenseId)).thenReturn(Optional.empty());
        when(outboxEventRepository.save(any(PharmacyDispenseAccountingOutboxEventEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        outboxService.enqueue(dispenseId, "DSP-20260903-ABCDEFGH");

        ArgumentCaptor<PharmacyDispenseAccountingOutboxEventEntity> eventCaptor = ArgumentCaptor
                .forClass(PharmacyDispenseAccountingOutboxEventEntity.class);
        verify(outboxEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getDispenseId()).isEqualTo(dispenseId);
        assertThat(eventCaptor.getValue().getStatus()).isEqualTo(
                com.hopital.patient.application.domain.AccountingOutboxStatus.PENDING);
    }

    @Test
    void doesNotCreateADuplicateForTheSameDispense() {
        UUID dispenseId = UUID.randomUUID();
        when(outboxEventRepository.findByDispenseId(dispenseId)).thenReturn(Optional.of(
                new PharmacyDispenseAccountingOutboxEventEntity(
                        UUID.randomUUID(), dispenseId, "DSP-20260903-ABCDEFGH", java.time.Instant.now())));

        outboxService.enqueue(dispenseId, "DSP-20260903-ABCDEFGH");

        verify(outboxEventRepository).findByDispenseId(dispenseId);
    }
}
