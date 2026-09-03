package com.hopital.accounting.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hopital.accounting.application.domain.AccountNature;
import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingEntryStatus;
import com.hopital.accounting.application.domain.AccountingSourceType;
import com.hopital.accounting.application.domain.JournalType;
import com.hopital.accounting.infra.integration.organization.HospitalReferenceClient;
import com.hopital.accounting.infra.integration.patient.PatientAccountingReferenceClient;
import com.hopital.accounting.infra.integration.pharmacy.PharmacyAccountingReferenceClient;
import com.hopital.accounting.infra.persistence.entity.AccountingAccountEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingEntryEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingJournalEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingPeriodEntity;
import com.hopital.accounting.infra.persistence.repository.AccountingAccountRepository;
import com.hopital.accounting.infra.persistence.repository.AccountingEntryLineRepository;
import com.hopital.accounting.infra.persistence.repository.AccountingEntryRepository;
import com.hopital.accounting.infra.persistence.repository.AccountingInvoiceRepository;
import com.hopital.accounting.infra.persistence.repository.AccountingJournalRepository;
import com.hopital.accounting.infra.persistence.repository.AccountingPaymentRepository;
import com.hopital.accounting.infra.persistence.repository.AccountingPeriodRepository;
import com.hopital.accounting.infra.persistence.repository.AccountingSupportingDocumentRepository;
import com.hopital.accounting.infra.persistence.repository.CashSessionRepository;
import com.hopital.accounting.infra.persistence.repository.FinancialStatementNoteRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PharmacyStockReceiptAccountingTest {

    @Mock private AccountingAccountRepository accountRepository;
    @Mock private AccountingJournalRepository journalRepository;
    @Mock private AccountingPeriodRepository periodRepository;
    @Mock private AccountingEntryRepository entryRepository;
    @Mock private AccountingEntryLineRepository entryLineRepository;
    @Mock private AccountingInvoiceRepository invoiceRepository;
    @Mock private AccountingPaymentRepository paymentRepository;
    @Mock private FinancialStatementNoteRepository noteRepository;
    @Mock private AccountingSupportingDocumentRepository documentRepository;
    @Mock private CashSessionRepository cashSessionRepository;
    @Mock private HospitalReferenceClient hospitalReferenceClient;
    @Mock private PatientAccountingReferenceClient patientReferenceClient;
    @Mock private PharmacyAccountingReferenceClient pharmacyReferenceClient;

    @Test
    void postsSupplierStockReceiptFromTheImmutablePharmacyReference() {
        UUID hospitalId = UUID.randomUUID();
        Instant receivedAt = Instant.parse("2026-09-03T10:15:30Z");
        AccountingAccountEntity stock = account(hospitalId, "310000", "Stock de médicaments", AccountNature.ASSET);
        AccountingAccountEntity supplier = account(hospitalId, "401100", "Fournisseurs", AccountNature.LIABILITY);
        AccountingAccountEntity clearing = account(hospitalId, "408100", "Réceptions à rapprocher", AccountNature.LIABILITY);
        AccountingJournalEntity purchases = new AccountingJournalEntity(UUID.randomUUID(), hospitalId, "HOP-01", "ACH",
                "Achats", JournalType.PURCHASES, true, receivedAt);
        LocalDate date = LocalDate.of(2026, 9, 3);
        AccountingPeriodEntity period = new AccountingPeriodEntity(UUID.randomUUID(), hospitalId, "HOP-01", "EX-2026",
                "Exercice 2026", date.minusDays(1), date.plusDays(1), receivedAt);

        when(pharmacyReferenceClient.resolveStockEntry("ENT-20260903-ABCD1234")).thenReturn(
                new PharmacyAccountingReferenceClient.PharmacyStockEntryReference(
                        UUID.randomUUID(), "ENT-20260903-ABCD1234", hospitalId, "HOP-01", "Fournisseur test",
                        new BigDecimal("125.50"), "CDF", receivedAt, "pharmacien-1", "pharmacien"));
        when(hospitalReferenceClient.resolveActive(hospitalId))
                .thenReturn(new HospitalReferenceClient.HospitalReference(hospitalId, "HOP-01", true));
        when(accountRepository.findAllByHospitalIdOrderByAccountNumberAsc(hospitalId)).thenReturn(List.of(stock, supplier, clearing));
        when(journalRepository.findAllByHospitalIdOrderByCodeAsc(hospitalId)).thenReturn(List.of(purchases));
        when(periodRepository.existsByHospitalIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(eq(hospitalId), eq(LocalDate.now()), eq(LocalDate.now())))
                .thenReturn(true);
        when(entryRepository.findByHospitalIdAndSourceTypeAndSourceCode(hospitalId,
                AccountingSourceType.PHARMACY_STOCK_RECEIPT, "ENT-20260903-ABCD1234")).thenReturn(Optional.empty());
        when(accountRepository.findByHospitalIdAndAccountNumber(hospitalId, "310000")).thenReturn(Optional.of(stock));
        when(accountRepository.findByHospitalIdAndAccountNumber(hospitalId, "401100")).thenReturn(Optional.of(supplier));
        when(journalRepository.findByHospitalIdAndCode(hospitalId, "ACH")).thenReturn(Optional.of(purchases));
        when(periodRepository.findOpenContaining(hospitalId, date)).thenReturn(Optional.of(period));
        when(entryRepository.existsByHospitalIdAndCode(eq(hospitalId), any())).thenReturn(false);
        when(entryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(entryLineRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AccountingApplicationService service = new AccountingApplicationService(accountRepository, journalRepository, periodRepository,
                entryRepository, entryLineRepository, invoiceRepository, paymentRepository, noteRepository, documentRepository,
                cashSessionRepository, hospitalReferenceClient, patientReferenceClient, pharmacyReferenceClient);

        var response = service.recordPharmacyStockReceipt("ENT-20260903-ABCD1234");

        assertThat(response.alreadyRecorded()).isFalse();
        assertThat(response.accountingEntryReference()).isNotBlank();
        ArgumentCaptor<AccountingEntryEntity> entryCaptor = ArgumentCaptor.forClass(AccountingEntryEntity.class);
        verify(entryRepository).save(entryCaptor.capture());
        assertThat(entryCaptor.getValue().getSourceType()).isEqualTo(AccountingSourceType.PHARMACY_STOCK_RECEIPT);
        assertThat(entryCaptor.getValue().getTotalDebit()).isEqualByComparingTo("125.50");
        assertThat(entryCaptor.getValue().getTotalCredit()).isEqualByComparingTo("125.50");
        assertThat(entryCaptor.getValue().getStatus()).isEqualTo(AccountingEntryStatus.POSTED);
        verify(accountRepository).findByHospitalIdAndAccountNumber(hospitalId, "401100");
    }

    private AccountingAccountEntity account(UUID hospitalId, String number, String label, AccountNature nature) {
        return new AccountingAccountEntity(UUID.randomUUID(), hospitalId, "HOP-01", number, label,
                number.substring(0, 1), nature, true, Instant.now());
    }
}
