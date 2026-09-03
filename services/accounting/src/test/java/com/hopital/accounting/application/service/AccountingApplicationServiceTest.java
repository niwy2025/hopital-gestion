package com.hopital.accounting.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hopital.accounting.application.domain.AccountNature;
import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingEntryStatus;
import com.hopital.accounting.application.domain.AuditActor;
import com.hopital.accounting.application.domain.CashSessionStatus;
import com.hopital.accounting.application.domain.DataAccessScope;
import com.hopital.accounting.application.domain.JournalType;
import com.hopital.accounting.application.dto.CreateAccountingEntryLineRequest;
import com.hopital.accounting.application.dto.CreateAccountingEntryRequest;
import com.hopital.accounting.application.dto.CloseCashSessionRequest;
import com.hopital.accounting.application.exception.AccountingAccessDeniedException;
import com.hopital.accounting.application.exception.AccountingValidationException;
import com.hopital.accounting.infra.integration.organization.HospitalReferenceClient;
import com.hopital.accounting.infra.integration.patient.PatientAccountingReferenceClient;
import com.hopital.accounting.infra.integration.pharmacy.PharmacyAccountingReferenceClient;
import com.hopital.accounting.infra.persistence.entity.AccountingAccountEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingJournalEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingPeriodEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingEntryLineEntity;
import com.hopital.accounting.infra.persistence.entity.CashSessionEntity;
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
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class AccountingApplicationServiceTest {
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
    @Mock private PharmacyPaymentSettlementOutboxService pharmacyPaymentSettlementOutboxService;

    private AccountingApplicationService service;
    private final UUID hospitalId = UUID.randomUUID();
    private final UUID debitAccountId = UUID.randomUUID();
    private final UUID creditAccountId = UUID.randomUUID();
    private final UUID journalId = UUID.randomUUID();
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        service = new AccountingApplicationService(accountRepository, journalRepository, periodRepository, entryRepository,
                entryLineRepository, invoiceRepository, paymentRepository, noteRepository, documentRepository, cashSessionRepository,
                hospitalReferenceClient, patientReferenceClient, pharmacyReferenceClient, pharmacyPaymentSettlementOutboxService);
    }

    @Test
    void createsBalancedManualEntryAsDraft() {
        AccountingAccountEntity debit = account(debitAccountId, "571100", "Caisse", AccountNature.ASSET);
        AccountingAccountEntity credit = account(creditAccountId, "706100", "Prestations", AccountNature.REVENUE);
        AccountingJournalEntity journal = new AccountingJournalEntity(journalId, hospitalId, "HOP-01", "OD", "OD", JournalType.GENERAL, true, Instant.now());
        AccountingPeriodEntity period = new AccountingPeriodEntity(UUID.randomUUID(), hospitalId, "HOP-01", "EX-TEST", "Exercice", today.minusDays(1), today.plusDays(1), Instant.now());
        when(accountRepository.findAllByHospitalIdOrderByAccountNumberAsc(hospitalId)).thenReturn(List.of(debit, credit));
        when(journalRepository.findAllByHospitalIdOrderByCodeAsc(hospitalId)).thenReturn(List.of(journal));
        when(periodRepository.findOpenContaining(hospitalId, today)).thenReturn(Optional.of(period));
        when(journalRepository.findById(journalId)).thenReturn(Optional.of(journal));
        when(accountRepository.findById(debitAccountId)).thenReturn(Optional.of(debit));
        when(accountRepository.findById(creditAccountId)).thenReturn(Optional.of(credit));
        when(entryRepository.existsByHospitalIdAndCode(eq(hospitalId), any())).thenReturn(false);
        when(entryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(entryLineRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createManualEntry(new CreateAccountingEntryRequest(null, journalId, today, "Ajustement de caisse",
                        AccountingCurrency.CDF, List.of(
                                new CreateAccountingEntryLineRequest(debitAccountId, "Entrée de caisse", new BigDecimal("100.00"), BigDecimal.ZERO, null),
                                new CreateAccountingEntryLineRequest(creditAccountId, "Produit", BigDecimal.ZERO, new BigDecimal("100.00"), null))),
                new DataAccessScope(false, false, hospitalId, "HOP-01"), new AuditActor("user-1", "comptable"));

        assertThat(response.status()).isEqualTo(AccountingEntryStatus.DRAFT);
        assertThat(response.totalDebit()).isEqualByComparingTo("100.00");
        assertThat(response.totalCredit()).isEqualByComparingTo("100.00");
        assertThat(response.lines()).hasSize(2);
        verify(entryRepository).save(any());
    }

    @Test
    void rejectsUnbalancedManualEntryBeforePersisting() {
        AccountingAccountEntity debit = account(debitAccountId, "571100", "Caisse", AccountNature.ASSET);
        AccountingAccountEntity credit = account(creditAccountId, "706100", "Prestations", AccountNature.REVENUE);
        AccountingJournalEntity journal = new AccountingJournalEntity(journalId, hospitalId, "HOP-01", "OD", "OD", JournalType.GENERAL, true, Instant.now());
        AccountingPeriodEntity period = new AccountingPeriodEntity(UUID.randomUUID(), hospitalId, "HOP-01", "EX-TEST", "Exercice", today.minusDays(1), today.plusDays(1), Instant.now());
        when(accountRepository.findAllByHospitalIdOrderByAccountNumberAsc(hospitalId)).thenReturn(List.of(debit, credit));
        when(journalRepository.findAllByHospitalIdOrderByCodeAsc(hospitalId)).thenReturn(List.of(journal));
        when(periodRepository.findOpenContaining(hospitalId, today)).thenReturn(Optional.of(period));
        when(journalRepository.findById(journalId)).thenReturn(Optional.of(journal));
        when(accountRepository.findById(debitAccountId)).thenReturn(Optional.of(debit));
        when(accountRepository.findById(creditAccountId)).thenReturn(Optional.of(credit));

        assertThatThrownBy(() -> service.createManualEntry(new CreateAccountingEntryRequest(null, journalId, today, "Erreur",
                        AccountingCurrency.CDF, List.of(
                                new CreateAccountingEntryLineRequest(debitAccountId, null, new BigDecimal("100.00"), BigDecimal.ZERO, null),
                                new CreateAccountingEntryLineRequest(creditAccountId, null, BigDecimal.ZERO, new BigDecimal("99.00"), null))),
                new DataAccessScope(false, false, hospitalId, "HOP-01"), new AuditActor("user-1", "comptable")))
                .isInstanceOf(AccountingValidationException.class)
                .hasMessageContaining("équilibrée");
    }

    @Test
    void deniesProvinceWideNonAdministratorWithoutHospitalAssignment() {
        assertThatThrownBy(() -> service.searchAccounts(0, 20, null, null, null,
                        new DataAccessScope(true, false, null, null)))
                .isInstanceOf(AccountingAccessDeniedException.class);
    }

    @Test
    void keepsReadAccessWhenTheCurrentPeriodIsClosed() {
        AccountingAccountEntity account = account(debitAccountId, "571100", "Caisse", AccountNature.ASSET);
        AccountingJournalEntity journal = new AccountingJournalEntity(journalId, hospitalId, "HOP-01", "OD", "OD",
                JournalType.GENERAL, true, Instant.now());
        when(accountRepository.findAllByHospitalIdOrderByAccountNumberAsc(hospitalId)).thenReturn(List.of(account));
        when(journalRepository.findAllByHospitalIdOrderByCodeAsc(hospitalId)).thenReturn(List.of(journal));
        when(periodRepository.existsByHospitalIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(eq(hospitalId), eq(today), eq(today)))
                .thenReturn(true);
        when(accountRepository.search(eq(hospitalId), eq(""), eq(null), any())).thenReturn(new PageImpl<>(List.of(account)));

        var response = service.searchAccounts(0, 20, null, null, null,
                new DataAccessScope(false, false, hospitalId, "HOP-01"));

        assertThat(response.content()).hasSize(1);
    }

    @Test
    void letsCentralAdministratorCloseCashSessionWithoutPersonalHospitalAssignment() {
        UUID cashSessionId = UUID.randomUUID();
        CashSessionEntity session = new CashSessionEntity(cashSessionId, hospitalId, "HOP-01", "CAI-TEST",
                AccountingCurrency.CDF, new BigDecimal("10.00"), "cashier-1", "caissier", Instant.now());
        when(cashSessionRepository.findById(cashSessionId)).thenReturn(Optional.of(session));

        var response = service.closeCashSession(cashSessionId, new CloseCashSessionRequest(new BigDecimal("10.00"), null),
                new DataAccessScope(true, true, null, null), new AuditActor("admin-1", "admin"));

        assertThat(response.status()).isEqualTo(CashSessionStatus.CLOSED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void recordsStockLossAgainstTheConfiguredLossAccount() {
        AccountingAccountEntity stock = account(UUID.randomUUID(), "310000", "Stock de médicaments", AccountNature.ASSET);
        AccountingAccountEntity loss = account(UUID.randomUUID(), "658100", "Pertes et avaries de stock", AccountNature.EXPENSE);
        AccountingJournalEntity journal = new AccountingJournalEntity(journalId, hospitalId, "HOP-01", "OD", "OD",
                JournalType.GENERAL, true, Instant.now());
        AccountingPeriodEntity period = new AccountingPeriodEntity(UUID.randomUUID(), hospitalId, "HOP-01", "EX-TEST",
                "Exercice", today.minusDays(1), today.plusDays(1), Instant.now());
        when(pharmacyReferenceClient.resolveStockMovement("MVT-LOSS-001")).thenReturn(
                new PharmacyAccountingReferenceClient.PharmacyStockMovementReference(
                        UUID.randomUUID(), "MVT-LOSS-001", "LOSS", null, null, hospitalId, "HOP-01", 2,
                        new BigDecimal("12.50"), new BigDecimal("25.00"), "CDF", "Produit endommagé", Instant.now(),
                        "pharmacist-1", "pharmacien"));
        when(hospitalReferenceClient.resolveActive(hospitalId))
                .thenReturn(new HospitalReferenceClient.HospitalReference(hospitalId, "HOP-01", true));
        when(accountRepository.findAllByHospitalIdOrderByAccountNumberAsc(hospitalId)).thenReturn(List.of(stock, loss));
        when(journalRepository.findAllByHospitalIdOrderByCodeAsc(hospitalId)).thenReturn(List.of(journal));
        when(periodRepository.existsByHospitalIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(eq(hospitalId), any(), any()))
                .thenReturn(true);
        when(entryRepository.findByHospitalIdAndSourceTypeAndSourceCode(
                hospitalId, com.hopital.accounting.application.domain.AccountingSourceType.PHARMACY_STOCK_MOVEMENT,
                "MVT-LOSS-001")).thenReturn(Optional.empty());
        when(periodRepository.findOpenContaining(eq(hospitalId), any())).thenReturn(Optional.of(period));
        when(journalRepository.findByHospitalIdAndCode(hospitalId, "OD")).thenReturn(Optional.of(journal));
        when(accountRepository.findByHospitalIdAndAccountNumber(hospitalId, "310000")).thenReturn(Optional.of(stock));
        when(accountRepository.findByHospitalIdAndAccountNumber(hospitalId, "658100")).thenReturn(Optional.of(loss));
        when(entryRepository.existsByHospitalIdAndCode(eq(hospitalId), any())).thenReturn(false);
        when(entryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.recordPharmacyStockMovement("mvt-loss-001");

        assertThat(response.ignored()).isFalse();
        assertThat(response.alreadyRecorded()).isFalse();
        org.mockito.ArgumentCaptor<Iterable<AccountingEntryLineEntity>> linesCaptor = org.mockito.ArgumentCaptor
                .forClass(Iterable.class);
        verify(entryLineRepository).saveAll(linesCaptor.capture());
        var lines = StreamSupport.stream(linesCaptor.getValue().spliterator(), false).toList();
        assertThat(lines).extracting(AccountingEntryLineEntity::getAccountNumber)
                .containsExactly("658100", "310000");
        assertThat(lines.getFirst().getDebit()).isEqualByComparingTo("25.00");
        assertThat(lines.get(1).getCredit()).isEqualByComparingTo("25.00");
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendsManualDispensingToExplicitClearingInsteadOfRecognisingRevenue() {
        AccountingAccountEntity stock = account(UUID.randomUUID(), "310000", "Stock de médicaments", AccountNature.ASSET);
        AccountingAccountEntity clearing = account(UUID.randomUUID(), "471100", "Délivrances pharmacie à rapprocher", AccountNature.ASSET);
        AccountingJournalEntity journal = new AccountingJournalEntity(journalId, hospitalId, "HOP-01", "OD", "OD",
                JournalType.GENERAL, true, Instant.now());
        AccountingPeriodEntity period = new AccountingPeriodEntity(UUID.randomUUID(), hospitalId, "HOP-01", "EX-TEST",
                "Exercice", today.minusDays(1), today.plusDays(1), Instant.now());
        when(pharmacyReferenceClient.resolveStockMovement("MVT-DISP-001")).thenReturn(
                new PharmacyAccountingReferenceClient.PharmacyStockMovementReference(
                        UUID.randomUUID(), "MVT-DISP-001", "DISPENSING", null, null, hospitalId, "HOP-01", 1,
                        new BigDecimal("18.00"), new BigDecimal("18.00"), "CDF", null, Instant.now(),
                        "pharmacist-1", "pharmacien"));
        when(hospitalReferenceClient.resolveActive(hospitalId))
                .thenReturn(new HospitalReferenceClient.HospitalReference(hospitalId, "HOP-01", true));
        when(accountRepository.findAllByHospitalIdOrderByAccountNumberAsc(hospitalId)).thenReturn(List.of(stock, clearing));
        when(journalRepository.findAllByHospitalIdOrderByCodeAsc(hospitalId)).thenReturn(List.of(journal));
        when(periodRepository.existsByHospitalIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(eq(hospitalId), any(), any()))
                .thenReturn(true);
        when(entryRepository.findByHospitalIdAndSourceTypeAndSourceCode(
                hospitalId, com.hopital.accounting.application.domain.AccountingSourceType.PHARMACY_STOCK_MOVEMENT,
                "MVT-DISP-001")).thenReturn(Optional.empty());
        when(periodRepository.findOpenContaining(eq(hospitalId), any())).thenReturn(Optional.of(period));
        when(journalRepository.findByHospitalIdAndCode(hospitalId, "OD")).thenReturn(Optional.of(journal));
        when(accountRepository.findByHospitalIdAndAccountNumber(hospitalId, "310000")).thenReturn(Optional.of(stock));
        when(accountRepository.findByHospitalIdAndAccountNumber(hospitalId, "471100")).thenReturn(Optional.of(clearing));
        when(entryRepository.existsByHospitalIdAndCode(eq(hospitalId), any())).thenReturn(false);
        when(entryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.recordPharmacyStockMovement("MVT-DISP-001");

        org.mockito.ArgumentCaptor<Iterable<AccountingEntryLineEntity>> linesCaptor = org.mockito.ArgumentCaptor
                .forClass(Iterable.class);
        verify(entryLineRepository).saveAll(linesCaptor.capture());
        var lines = StreamSupport.stream(linesCaptor.getValue().spliterator(), false).toList();
        assertThat(lines).extracting(AccountingEntryLineEntity::getAccountNumber)
                .containsExactly("471100", "310000");
    }

    @Test
    void neverDuplicatesAStockMovementAlreadyLinkedToADspDispense() {
        when(pharmacyReferenceClient.resolveStockMovement("MVT-DSP-001")).thenReturn(
                new PharmacyAccountingReferenceClient.PharmacyStockMovementReference(
                        UUID.randomUUID(), "MVT-DSP-001", "DISPENSING", "PRESCRIPTION_DISPENSE", "DSP-20260903-0001",
                        hospitalId, "HOP-01", 1, new BigDecimal("18.00"), new BigDecimal("18.00"), "CDF", null,
                        Instant.now(), "pharmacist-1", "pharmacien"));

        var response = service.recordPharmacyStockMovement("MVT-DSP-001");

        assertThat(response.ignored()).isTrue();
        assertThat(response.ignoredReason()).contains("DSP");
        verifyNoInteractions(hospitalReferenceClient, entryRepository, entryLineRepository);
    }

    private AccountingAccountEntity account(UUID id, String number, String label, AccountNature nature) {
        return new AccountingAccountEntity(id, hospitalId, "HOP-01", number, label, number.substring(0, 1), nature, true, Instant.now());
    }
}
