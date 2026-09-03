package com.hopital.pharmacy.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hopital.pharmacy.application.domain.AuditActor;
import com.hopital.pharmacy.application.domain.Currency;
import com.hopital.pharmacy.application.domain.StockMovementSourceType;
import com.hopital.pharmacy.application.domain.StockMovementType;
import com.hopital.pharmacy.infra.integration.accounting.AccountingPharmacyStockMovementClient;
import com.hopital.pharmacy.infra.persistence.entity.HospitalStockEntity;
import com.hopital.pharmacy.infra.persistence.entity.MedicineEntity;
import com.hopital.pharmacy.infra.persistence.entity.StockMovementAccountingOutboxEventEntity;
import com.hopital.pharmacy.infra.persistence.entity.StockMovementEntity;
import com.hopital.pharmacy.infra.persistence.repository.StockMovementAccountingOutboxEventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockMovementAccountingOutboxTest {

    @Mock
    private StockMovementAccountingOutboxEventRepository outboxEventRepository;

    @Mock
    private AccountingPharmacyStockMovementClient accountingClient;

    @Test
    void queuesOneDurableEventForALossAndAvoidsDuplicates() {
        StockMovementAccountingOutboxService service = new StockMovementAccountingOutboxService(outboxEventRepository);
        StockMovementEntity movement = movement(StockMovementType.LOSS, null, null);
        when(outboxEventRepository.findByStockMovementId(movement.getId())).thenReturn(Optional.empty());

        service.enqueueIfRequired(movement);

        ArgumentCaptor<StockMovementAccountingOutboxEventEntity> eventCaptor = ArgumentCaptor
                .forClass(StockMovementAccountingOutboxEventEntity.class);
        verify(outboxEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getStockMovementId()).isEqualTo(movement.getId());
        assertThat(eventCaptor.getValue().getStockMovementCode()).isEqualTo(movement.getCode());

        when(outboxEventRepository.findByStockMovementId(movement.getId()))
                .thenReturn(Optional.of(eventCaptor.getValue()));
        service.enqueueIfRequired(movement);
        verify(outboxEventRepository, times(1)).save(any(StockMovementAccountingOutboxEventEntity.class));
    }

    @Test
    void neverQueuesTheDspMovementHandledByThePatientOutbox() {
        StockMovementAccountingOutboxService service = new StockMovementAccountingOutboxService(outboxEventRepository);

        service.enqueueIfRequired(movement(
                StockMovementType.DISPENSING,
                StockMovementSourceType.PRESCRIPTION_DISPENSE,
                "DSP-20260903-001"));

        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    void marksAnAcknowledgedMovementAsPosted() {
        StockMovementAccountingOutboxDispatcher dispatcher = new StockMovementAccountingOutboxDispatcher(
                outboxEventRepository, accountingClient);
        StockMovementAccountingOutboxEventEntity event = new StockMovementAccountingOutboxEventEntity(
                UUID.randomUUID(), UUID.randomUUID(), "MVT-LOSS-001", Instant.now().minusSeconds(1));
        when(outboxEventRepository.lockByStockMovementCode(event.getStockMovementCode())).thenReturn(Optional.of(event));
        when(accountingClient.postStockMovement(event.getStockMovementCode())).thenReturn(
                new AccountingPharmacyStockMovementClient.AccountingPostingAcknowledgement("ECR-001", false, null));

        dispatcher.deliverDue(event.getStockMovementCode());

        assertThat(event.getStatus().name()).isEqualTo("POSTED");
        assertThat(event.getAccountingEntryReference()).isEqualTo("ECR-001");
    }

    @Test
    void marksADspAcknowledgementAsExcludedInsteadOfRetryingForever() {
        StockMovementAccountingOutboxDispatcher dispatcher = new StockMovementAccountingOutboxDispatcher(
                outboxEventRepository, accountingClient);
        StockMovementAccountingOutboxEventEntity event = new StockMovementAccountingOutboxEventEntity(
                UUID.randomUUID(), UUID.randomUUID(), "MVT-DSP-001", Instant.now().minusSeconds(1));
        when(outboxEventRepository.lockByStockMovementCode(event.getStockMovementCode())).thenReturn(Optional.of(event));
        when(accountingClient.postStockMovement(event.getStockMovementCode())).thenReturn(
                new AccountingPharmacyStockMovementClient.AccountingPostingAcknowledgement(
                        null, true, "Déjà traité par le flux DSP."));

        dispatcher.deliverDue(event.getStockMovementCode());

        assertThat(event.getStatus().name()).isEqualTo("EXCLUDED");
        assertThat(event.getAccountingEntryReference()).isNull();
    }

    private StockMovementEntity movement(
            StockMovementType type,
            StockMovementSourceType sourceType,
            String sourceCode) {
        AuditActor actor = new AuditActor("user-1", "pharmacien");
        MedicineEntity medicine = new MedicineEntity(UUID.randomUUID(), "MED-001", "Paracétamol", null, null, null, null,
                actor, Instant.now());
        HospitalStockEntity stock = new HospitalStockEntity(UUID.randomUUID(), UUID.randomUUID(), "HOP-01", medicine, 20,
                0, new BigDecimal("10.00"), new BigDecimal("15.00"), Currency.CDF, Instant.now());
        return new StockMovementEntity(UUID.randomUUID(), "MVT-LOSS-001", stock, null, type, sourceType, sourceCode, 2,
                new BigDecimal("10.00"), null, Currency.CDF, null, actor, Instant.now());
    }
}
