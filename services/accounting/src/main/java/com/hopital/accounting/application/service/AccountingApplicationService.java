package com.hopital.accounting.application.service;

import com.hopital.accounting.application.domain.AccountNature;
import com.hopital.accounting.application.domain.AccountingCurrency;
import com.hopital.accounting.application.domain.AccountingEntryStatus;
import com.hopital.accounting.application.domain.AccountingPaymentMethod;
import com.hopital.accounting.application.domain.AccountingPeriodStatus;
import com.hopital.accounting.application.domain.AccountingSourceType;
import com.hopital.accounting.application.domain.AuditActor;
import com.hopital.accounting.application.domain.CashSessionStatus;
import com.hopital.accounting.application.domain.DataAccessScope;
import com.hopital.accounting.application.domain.FinancialStatementNoteStatus;
import com.hopital.accounting.application.domain.InvoiceStatus;
import com.hopital.accounting.application.domain.JournalType;
import com.hopital.accounting.application.dto.AccountingAccountResponse;
import com.hopital.accounting.application.dto.AccountingEntryLineResponse;
import com.hopital.accounting.application.dto.AccountingEntryResponse;
import com.hopital.accounting.application.dto.AccountingInvoiceResponse;
import com.hopital.accounting.application.dto.AccountingJournalResponse;
import com.hopital.accounting.application.dto.AccountingPaymentResponse;
import com.hopital.accounting.application.dto.AccountingPeriodResponse;
import com.hopital.accounting.application.dto.AccountingSupportingDocumentResponse;
import com.hopital.accounting.application.dto.AccountingDashboardResponse;
import com.hopital.accounting.application.dto.CashSessionResponse;
import com.hopital.accounting.application.dto.CloseCashSessionRequest;
import com.hopital.accounting.application.dto.CreateAccountingAccountRequest;
import com.hopital.accounting.application.dto.CreateAccountingEntryLineRequest;
import com.hopital.accounting.application.dto.CreateAccountingEntryRequest;
import com.hopital.accounting.application.dto.CreateAccountingInvoiceRequest;
import com.hopital.accounting.application.dto.CreateAccountingJournalRequest;
import com.hopital.accounting.application.dto.CreateAccountingPaymentRequest;
import com.hopital.accounting.application.dto.CreateAccountingPeriodRequest;
import com.hopital.accounting.application.dto.CreateFinancialStatementNoteRequest;
import com.hopital.accounting.application.dto.FinancialStatementNoteResponse;
import com.hopital.accounting.application.dto.FinancialStatementSectionResponse;
import com.hopital.accounting.application.dto.FinancialStatementsResponse;
import com.hopital.accounting.application.dto.LedgerRowResponse;
import com.hopital.accounting.application.dto.OpenCashSessionRequest;
import com.hopital.accounting.application.dto.PageResponse;
import com.hopital.accounting.application.dto.PharmacyDispensationAccountingResponse;
import com.hopital.accounting.application.dto.PharmacyStockReceiptAccountingResponse;
import com.hopital.accounting.application.dto.PharmacyStockMovementAccountingResponse;
import com.hopital.accounting.application.dto.ReverseAccountingEntryRequest;
import com.hopital.accounting.application.dto.TrialBalanceLineResponse;
import com.hopital.accounting.application.dto.TrialBalanceResponse;
import com.hopital.accounting.application.dto.UpdateAccountingAccountRequest;
import com.hopital.accounting.application.dto.UpdateAccountingJournalRequest;
import com.hopital.accounting.application.dto.UpdateFinancialStatementNoteRequest;
import com.hopital.accounting.application.dto.UploadAccountingSupportingDocumentRequest;
import com.hopital.accounting.application.exception.AccountingResourceNotFoundException;
import com.hopital.accounting.application.exception.AccountingAccessDeniedException;
import com.hopital.accounting.application.exception.AccountingValidationException;
import com.hopital.accounting.infra.integration.organization.HospitalReferenceClient;
import com.hopital.accounting.infra.integration.patient.PatientAccountingReferenceClient;
import com.hopital.accounting.infra.integration.pharmacy.PharmacyAccountingReferenceClient;
import com.hopital.accounting.infra.persistence.entity.AccountingAccountEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingEntryEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingEntryLineEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingInvoiceEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingJournalEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingPaymentEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingPeriodEntity;
import com.hopital.accounting.infra.persistence.entity.AccountingSupportingDocumentEntity;
import com.hopital.accounting.infra.persistence.entity.CashSessionEntity;
import com.hopital.accounting.infra.persistence.entity.FinancialStatementNoteEntity;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hospital-scoped accounting core. The initial chart is an intentionally
 * configurable SYSCOHADA-oriented baseline, not a claim of statutory
 * certification. Posted vouchers and validated annexes are append-only.
 */
@Service
@Transactional
public class AccountingApplicationService {
    private static final String ACCOUNT_RECEIVABLES = "411100";
    private static final String ACCOUNT_CASH = "571100";
    private static final String ACCOUNT_BANK = "521100";
    private static final String ACCOUNT_MOBILE_MONEY = "531100";
    private static final String ACCOUNT_MEDICINE_STOCK = "310000";
    private static final String ACCOUNT_STOCK_RECEIPT_CLEARING = "408100";
    private static final String ACCOUNT_MEDICINE_COST = "603100";
    // These accounts remain configurable by each hospital's finance manager.
    // They deliberately keep an unresolved business situation visible instead
    // of fabricating a sale, a supplier, or a transfer destination.
    private static final String ACCOUNT_STOCK_TRANSFER_CLEARING = "382000";
    private static final String ACCOUNT_MANUAL_DISPENSE_CLEARING = "471100";
    private static final String ACCOUNT_MEDICINE_LOSS = "658100";
    private static final String ACCOUNT_MEDICINE_EXPIRY = "658200";
    private static final String ACCOUNT_CARE_REVENUE = "706100";
    private static final String ACCOUNT_PHARMACY_REVENUE = "707100";

    private final AccountingAccountRepository accountRepository;
    private final AccountingJournalRepository journalRepository;
    private final AccountingPeriodRepository periodRepository;
    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository entryLineRepository;
    private final AccountingInvoiceRepository invoiceRepository;
    private final AccountingPaymentRepository paymentRepository;
    private final FinancialStatementNoteRepository noteRepository;
    private final AccountingSupportingDocumentRepository documentRepository;
    private final CashSessionRepository cashSessionRepository;
    private final HospitalReferenceClient hospitalReferenceClient;
    private final PatientAccountingReferenceClient patientReferenceClient;
    private final PharmacyAccountingReferenceClient pharmacyReferenceClient;
    private final PharmacyPaymentSettlementOutboxService pharmacyPaymentSettlementOutboxService;

    public AccountingApplicationService(AccountingAccountRepository accountRepository,
            AccountingJournalRepository journalRepository, AccountingPeriodRepository periodRepository,
            AccountingEntryRepository entryRepository, AccountingEntryLineRepository entryLineRepository,
            AccountingInvoiceRepository invoiceRepository, AccountingPaymentRepository paymentRepository,
            FinancialStatementNoteRepository noteRepository, AccountingSupportingDocumentRepository documentRepository,
            CashSessionRepository cashSessionRepository, HospitalReferenceClient hospitalReferenceClient,
            PatientAccountingReferenceClient patientReferenceClient, PharmacyAccountingReferenceClient pharmacyReferenceClient,
            PharmacyPaymentSettlementOutboxService pharmacyPaymentSettlementOutboxService) {
        this.accountRepository = accountRepository; this.journalRepository = journalRepository; this.periodRepository = periodRepository;
        this.entryRepository = entryRepository; this.entryLineRepository = entryLineRepository; this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository; this.noteRepository = noteRepository; this.documentRepository = documentRepository;
        this.cashSessionRepository = cashSessionRepository; this.hospitalReferenceClient = hospitalReferenceClient;
        this.patientReferenceClient = patientReferenceClient; this.pharmacyReferenceClient = pharmacyReferenceClient;
        this.pharmacyPaymentSettlementOutboxService = pharmacyPaymentSettlementOutboxService;
    }

    public PageResponse<AccountingAccountResponse> searchAccounts(int page, int size, String query, Boolean active,
            UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId);
        ensureHospitalBook(hospital);
        return page(accountRepository.search(hospital.id(), normalize(query), active, pageable(page, size, "accountNumber"))
                .map(this::toAccount));
    }

    @Transactional
    public AccountingAccountResponse createAccount(CreateAccountingAccountRequest request, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, request.hospitalId()); ensureHospitalBook(hospital);
        String number = request.accountNumber().trim().toUpperCase(Locale.ROOT);
        if (accountRepository.existsByHospitalIdAndAccountNumber(hospital.id(), number)) {
            throw new AccountingValidationException("Ce numéro de compte existe déjà dans cet hôpital.");
        }
        return toAccount(accountRepository.save(new AccountingAccountEntity(UUID.randomUUID(), hospital.id(), hospital.code(), number,
                request.label().trim(), number.substring(0, 1), request.nature(), false, Instant.now())));
    }

    @Transactional
    public AccountingAccountResponse updateAccount(UUID accountId, UpdateAccountingAccountRequest request, DataAccessScope scope) {
        AccountingAccountEntity account = ownedAccount(accountId, scope);
        if (account.isSystemAccount() && !request.active()) {
            throw new AccountingValidationException("Un compte utilisé par les écritures automatiques ne peut pas être désactivé.");
        }
        account.update(request.label().trim(), request.nature(), request.active());
        return toAccount(account);
    }

    public PageResponse<AccountingJournalResponse> searchJournals(int page, int size, String query, Boolean active,
            UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        return page(journalRepository.search(hospital.id(), normalize(query), active, pageable(page, size, "code")).map(this::toJournal));
    }

    @Transactional
    public AccountingJournalResponse createJournal(CreateAccountingJournalRequest request, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, request.hospitalId()); ensureHospitalBook(hospital);
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (journalRepository.existsByHospitalIdAndCode(hospital.id(), code)) {
            throw new AccountingValidationException("Ce code journal existe déjà dans cet hôpital.");
        }
        return toJournal(journalRepository.save(new AccountingJournalEntity(UUID.randomUUID(), hospital.id(), hospital.code(), code,
                request.label().trim(), request.type(), false, Instant.now())));
    }

    @Transactional
    public AccountingJournalResponse updateJournal(UUID journalId, UpdateAccountingJournalRequest request, DataAccessScope scope) {
        AccountingJournalEntity journal = ownedJournal(journalId, scope);
        if (journal.isSystemJournal() && !request.active()) {
            throw new AccountingValidationException("Un journal utilisé automatiquement ne peut pas être désactivé.");
        }
        journal.update(request.label().trim(), request.type(), request.active());
        return toJournal(journal);
    }

    public PageResponse<AccountingPeriodResponse> searchPeriods(int page, int size, String query, AccountingPeriodStatus status,
            UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        return page(periodRepository.search(hospital.id(), normalize(query), status, pageable(page, size, "startsOn")).map(this::toPeriod));
    }

    @Transactional
    public AccountingPeriodResponse createPeriod(CreateAccountingPeriodRequest request, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, request.hospitalId()); ensureHospitalBook(hospital);
        if (request.endsOn().isBefore(request.startsOn())) throw new AccountingValidationException("La fin de période précède son début.");
        for (AccountingPeriodEntity current : periodRepository.findAllByHospitalId(hospital.id())) {
            if (!request.endsOn().isBefore(current.getStartsOn()) && !request.startsOn().isAfter(current.getEndsOn())) {
                throw new AccountingValidationException("Cette période chevauche la période " + current.getCode() + ".");
            }
        }
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (periodRepository.findByHospitalIdAndCode(hospital.id(), code).isPresent()) {
            throw new AccountingValidationException("Ce code période existe déjà dans cet hôpital.");
        }
        return toPeriod(periodRepository.save(new AccountingPeriodEntity(UUID.randomUUID(), hospital.id(), hospital.code(), code,
                request.label().trim(), request.startsOn(), request.endsOn(), Instant.now())));
    }

    @Transactional
    public AccountingPeriodResponse closePeriod(UUID periodId, DataAccessScope scope, AuditActor actor) {
        AccountingPeriodEntity period = ownedPeriod(periodId, scope);
        if (period.getStatus() == AccountingPeriodStatus.CLOSED) return toPeriod(period);
        if (entryRepository.countByPeriodIdAndStatus(periodId, AccountingEntryStatus.DRAFT) > 0) {
            throw new AccountingValidationException("Les écritures brouillon doivent être postées ou supprimées avant la clôture.");
        }
        period.close(actor.userId(), actor.username(), Instant.now());
        return toPeriod(period);
    }

    public PageResponse<AccountingEntryResponse> searchEntries(int page, int size, String query, AccountingEntryStatus status,
            UUID periodId, LocalDate dateFrom, LocalDate dateTo, UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        return page(entryRepository.search(hospital.id(), normalize(query), status, periodId, dateFrom, dateTo,
                pageable(page, size, "entryDate")).map(entry -> toEntry(entry, List.of())));
    }

    public AccountingEntryResponse getEntry(UUID entryId, DataAccessScope scope) {
        AccountingEntryEntity entry = ownedEntry(entryId, scope);
        return toEntry(entry, entryLineRepository.findAllByEntry_IdOrderByLineNumberAsc(entry.getId()));
    }

    @Transactional
    public AccountingEntryResponse createManualEntry(CreateAccountingEntryRequest request, DataAccessScope scope, AuditActor actor) {
        HospitalContext hospital = hospital(scope, request.hospitalId()); ensureHospitalBook(hospital);
        AccountingPeriodEntity period = openPeriod(hospital, request.entryDate());
        AccountingJournalEntity journal = journalRepository.findById(request.journalId())
                .filter(item -> item.getHospitalId().equals(hospital.id()) && item.isActive())
                .orElseThrow(() -> new AccountingValidationException("Le journal sélectionné est indisponible."));
        List<ResolvedLine> lines = resolveAndValidateLines(hospital, request.lines());
        BigDecimal totalDebit = lines.stream().map(ResolvedLine::debit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream().map(ResolvedLine::credit).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertBalanced(totalDebit, totalCredit);
        String code = nextCode("ECR", value -> entryRepository.existsByHospitalIdAndCode(hospital.id(), value));
        AccountingEntryEntity entry = entryRepository.save(new AccountingEntryEntity(UUID.randomUUID(), hospital.id(), hospital.code(),
                period.getId(), journal.getId(), journal.getCode(), code, AccountingSourceType.MANUAL_ENTRY, code,
                request.entryDate(), request.description().trim(), request.currency(), money(totalDebit), money(totalCredit),
                actor.userId(), actor.username(), Instant.now()));
        List<AccountingEntryLineEntity> saved = saveLines(entry, lines);
        return toEntry(entry, saved);
    }

    @Transactional
    public AccountingEntryResponse postEntry(UUID entryId, DataAccessScope scope, AuditActor actor) {
        AccountingEntryEntity entry = ownedEntry(entryId, scope);
        if (entry.getStatus() != AccountingEntryStatus.DRAFT) {
            throw new AccountingValidationException("Seule une écriture brouillon peut être postée.");
        }
        AccountingPeriodEntity period = periodRepository.findById(entry.getPeriodId())
                .orElseThrow(() -> new AccountingResourceNotFoundException("La période comptable"));
        if (period.getStatus() != AccountingPeriodStatus.OPEN) throw new AccountingValidationException("La période comptable est clôturée.");
        List<AccountingEntryLineEntity> lines = entryLineRepository.findAllByEntry_IdOrderByLineNumberAsc(entryId);
        if (lines.size() < 2) throw new AccountingValidationException("Une écriture doit comporter au moins deux lignes.");
        assertBalanced(lines.stream().map(AccountingEntryLineEntity::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add),
                lines.stream().map(AccountingEntryLineEntity::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add));
        entry.post(actor.userId(), actor.username(), Instant.now());
        return toEntry(entry, lines);
    }

    @Transactional
    public AccountingEntryResponse reverseEntry(UUID entryId, ReverseAccountingEntryRequest request, DataAccessScope scope, AuditActor actor) {
        AccountingEntryEntity original = ownedEntry(entryId, scope);
        if (!original.isPosted()) throw new AccountingValidationException("Seule une écriture postée peut être contrepassée.");
        AccountingPeriodEntity period = openPeriod(new HospitalContext(original.getHospitalId(), original.getHospitalCode()), LocalDate.now());
        AccountingJournalEntity journal = journalRepository.findById(original.getJournalId())
                .orElseThrow(() -> new AccountingResourceNotFoundException("Le journal comptable"));
        List<AccountingEntryLineEntity> originalLines = entryLineRepository.findAllByEntry_IdOrderByLineNumberAsc(original.getId());
        String sourceCode = "REV-" + original.getCode() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String code = nextCode("ECR", value -> entryRepository.existsByHospitalIdAndCode(original.getHospitalId(), value));
        AccountingEntryEntity reversal = entryRepository.save(new AccountingEntryEntity(UUID.randomUUID(), original.getHospitalId(),
                original.getHospitalCode(), period.getId(), journal.getId(), journal.getCode(), code, AccountingSourceType.MANUAL_ENTRY,
                sourceCode, LocalDate.now(), "Contrepassation de " + original.getCode() + " : " + request.reason().trim(),
                original.getCurrency(), original.getTotalCredit(), original.getTotalDebit(), actor.userId(), actor.username(), Instant.now()));
        List<AccountingEntryLineEntity> reversalLines = new ArrayList<>();
        int line = 1;
        for (AccountingEntryLineEntity current : originalLines) {
            AccountingAccountEntity account = accountRepository.findById(current.getAccountId())
                    .orElseThrow(() -> new AccountingResourceNotFoundException("Le compte de contrepassation"));
            reversalLines.add(new AccountingEntryLineEntity(UUID.randomUUID(), reversal, line++, account,
                    "Contrepassation : " + current.getLabel(), current.getCredit(), current.getDebit(), current.getThirdPartyReference()));
        }
        entryLineRepository.saveAll(reversalLines);
        reversal.post(actor.userId(), actor.username(), Instant.now());
        original.markReversed(reversal.getId());
        return toEntry(reversal, reversalLines);
    }

    public PageResponse<AccountingInvoiceResponse> searchInvoices(int page, int size, String query, InvoiceStatus status,
            LocalDate dateFrom, LocalDate dateTo, UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        return page(invoiceRepository.search(hospital.id(), normalize(query), status, dateFrom, dateTo,
                pageable(page, size, "issuedOn")).map(this::toInvoice));
    }

    public AccountingInvoiceResponse getInvoice(UUID invoiceId, DataAccessScope scope) {
        return toInvoice(ownedInvoice(invoiceId, scope));
    }

    @Transactional
    public AccountingInvoiceResponse createManualInvoice(CreateAccountingInvoiceRequest request, DataAccessScope scope, AuditActor actor) {
        HospitalContext hospital = hospital(scope, request.hospitalId()); ensureHospitalBook(hospital);
        BigDecimal total = money(request.totalAmount());
        if (total.signum() <= 0) throw new AccountingValidationException("Le total d'une facture doit être supérieur à zéro.");
        String code = nextCode("FAC", value -> invoiceRepository.existsByHospitalIdAndCode(hospital.id(), value));
        AccountingInvoiceEntity invoice = invoiceRepository.save(new AccountingInvoiceEntity(UUID.randomUUID(), hospital.id(), hospital.code(),
                code, AccountingSourceType.MANUAL_INVOICE, code, request.patientId(), trimToNull(request.patientCode()), request.passageId(),
                trimToNull(request.passageCode()), request.issuedOn(), request.currency(), total, request.description().trim(),
                actor.userId(), actor.username(), Instant.now()));
        return toInvoice(invoice);
    }

    @Transactional
    public AccountingInvoiceResponse issueInvoice(UUID invoiceId, DataAccessScope scope, AuditActor actor) {
        AccountingInvoiceEntity invoice = ownedInvoice(invoiceId, scope);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) throw new AccountingValidationException("Cette facture a déjà été émise.");
        HospitalContext hospital = new HospitalContext(invoice.getHospitalId(), invoice.getHospitalCode()); ensureHospitalBook(hospital);
        invoice.issue();
        if (invoice.getTotalAmount().signum() > 0) {
            createAndPostEntry(hospital, invoice.getIssuedOn(), journal(hospital, "VEN"), AccountingSourceType.MANUAL_INVOICE,
                    invoice.getCode() + ":INVOICE", "Émission de la facture " + invoice.getCode(), invoice.getCurrency(), actor,
                    List.of(new LineSpec(account(hospital, ACCOUNT_RECEIVABLES), "Créance facture " + invoice.getCode(), invoice.getTotalAmount(), BigDecimal.ZERO, invoice.getPatientCode()),
                            new LineSpec(account(hospital, ACCOUNT_CARE_REVENUE), "Produit facture " + invoice.getCode(), BigDecimal.ZERO, invoice.getTotalAmount(), invoice.getPatientCode())));
        }
        return toInvoice(invoice);
    }

    @Transactional
    public AccountingInvoiceResponse cancelInvoice(UUID invoiceId, DataAccessScope scope, AuditActor actor) {
        AccountingInvoiceEntity invoice = ownedInvoice(invoiceId, scope);
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) return toInvoice(invoice);
        if (invoice.getSourceType() != AccountingSourceType.MANUAL_INVOICE) {
            throw new AccountingValidationException("Une facture issue d'une délivrance pharmacie ne peut pas être annulée manuellement.");
        }
        if (invoice.getStatus() != InvoiceStatus.DRAFT && invoice.getStatus() != InvoiceStatus.ISSUED) {
            throw new AccountingValidationException("Seule une facture brouillon ou émise non encaissée peut être annulée.");
        }
        HospitalContext hospital = new HospitalContext(invoice.getHospitalId(), invoice.getHospitalCode()); ensureHospitalBook(hospital);
        if (invoice.getStatus() == InvoiceStatus.ISSUED && invoice.getTotalAmount().signum() > 0) {
            createAndPostEntry(hospital, LocalDate.now(), journal(hospital, "VEN"), AccountingSourceType.MANUAL_INVOICE,
                    invoice.getCode() + ":CANCEL", "Annulation de la facture " + invoice.getCode(), invoice.getCurrency(), actor,
                    List.of(new LineSpec(account(hospital, ACCOUNT_CARE_REVENUE), "Annulation produit " + invoice.getCode(), invoice.getTotalAmount(), BigDecimal.ZERO, invoice.getPatientCode()),
                            new LineSpec(account(hospital, ACCOUNT_RECEIVABLES), "Annulation créance " + invoice.getCode(), BigDecimal.ZERO, invoice.getTotalAmount(), invoice.getPatientCode())));
        }
        invoice.cancel();
        return toInvoice(invoice);
    }

    public PageResponse<AccountingPaymentResponse> searchPayments(int page, int size, String query, AccountingPaymentMethod method, LocalDate dateFrom,
            LocalDate dateTo, UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        return page(paymentRepository.search(hospital.id(), normalize(query), method, dateFrom, dateTo,
                pageable(page, size, "paidOn")).map(this::toPayment));
    }

    @Transactional
    public AccountingPaymentResponse recordPayment(UUID invoiceId, CreateAccountingPaymentRequest request, DataAccessScope scope,
            AuditActor actor) {
        AccountingInvoiceEntity invoice = invoiceRepository.lockById(invoiceId)
                .orElseThrow(() -> new AccountingResourceNotFoundException("La facture"));
        assertOwned(invoice.getHospitalId(), scope);
        HospitalContext hospital = new HospitalContext(invoice.getHospitalId(), invoice.getHospitalCode()); ensureHospitalBook(hospital);
        String idempotencyKey = trimToNull(request.idempotencyKey());
        if (idempotencyKey != null) {
            AccountingPaymentEntity existing = paymentRepository.findByInvoiceIdAndIdempotencyKey(invoice.getId(), idempotencyKey)
                    .orElse(null);
            if (existing != null) {
                return toPayment(existing);
            }
        }
        if (invoice.getCurrency() != request.currency()) throw new AccountingValidationException("La devise du paiement doit correspondre à celle de la facture.");
        if (invoice.getDueAmount().compareTo(money(request.amount())) < 0) throw new AccountingValidationException("Le paiement dépasse le solde de la facture.");
        return toPayment(recordPayment(hospital, invoice, request.paidOn(), money(request.amount()), request.currency(), request.method(),
                trimToNull(request.paymentReference()), idempotencyKey, actor, AccountingSourceType.MANUAL_INVOICE,
                invoice.getCode() + ":PAYMENT:" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT)));
    }

    public PageResponse<LedgerRowResponse> searchLedger(int page, int size, String query, UUID accountId, UUID periodId,
            LocalDate dateFrom, LocalDate dateTo, UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        return page(entryLineRepository.ledger(hospital.id(), accountId, periodId, dateFrom, dateTo, normalize(query),
                PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by("entry.entryDate").descending()))
                .map(this::toLedgerRow));
    }

    public TrialBalanceResponse trialBalance(UUID periodId, LocalDate dateFrom, LocalDate dateTo, UUID requestedHospitalId,
            DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        List<AccountingAccountEntity> accounts = accountRepository.findAllByHospitalIdOrderByAccountNumberAsc(hospital.id());
        Map<UUID, Totals> totals = new LinkedHashMap<>();
        for (AccountingAccountEntity account : accounts) totals.put(account.getId(), new Totals(account, BigDecimal.ZERO, BigDecimal.ZERO));
        for (AccountingEntryLineEntity line : entryLineRepository.postedLines(hospital.id(), periodId, dateFrom, dateTo)) {
            Totals current = totals.get(line.getAccountId());
            if (current != null) totals.put(line.getAccountId(), current.add(line.getDebit(), line.getCredit()));
        }
        List<TrialBalanceLineResponse> result = new ArrayList<>();
        BigDecimal debit = BigDecimal.ZERO; BigDecimal credit = BigDecimal.ZERO;
        for (Totals current : totals.values()) {
            debit = debit.add(current.debit()); credit = credit.add(current.credit());
            BigDecimal net = current.debit().subtract(current.credit());
            result.add(new TrialBalanceLineResponse(current.account().getId(), current.account().getAccountNumber(),
                    current.account().getLabel(), current.account().getNature(), money(current.debit()), money(current.credit()),
                    net.signum() >= 0 ? money(net) : BigDecimal.ZERO.setScale(2),
                    net.signum() < 0 ? money(net.negate()) : BigDecimal.ZERO.setScale(2)));
        }
        return new TrialBalanceResponse(hospital.id(), periodId, dateFrom, dateTo, money(debit), money(credit), result);
    }

    public FinancialStatementsResponse financialStatements(UUID periodId, UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        TrialBalanceResponse balance = trialBalance(periodId, null, null, hospital.id(), scopeForHospital(hospital));
        BigDecimal assets = BigDecimal.ZERO, liabilities = BigDecimal.ZERO, equity = BigDecimal.ZERO, revenues = BigDecimal.ZERO, expenses = BigDecimal.ZERO;
        for (TrialBalanceLineResponse line : balance.lines()) {
            BigDecimal debitNet = line.totalDebit().subtract(line.totalCredit());
            switch (line.nature()) {
                case ASSET -> assets = assets.add(debitNet);
                case LIABILITY -> liabilities = liabilities.add(debitNet.negate());
                case EQUITY -> equity = equity.add(debitNet.negate());
                case REVENUE -> revenues = revenues.add(debitNet.negate());
                case EXPENSE -> expenses = expenses.add(debitNet);
            }
        }
        BigDecimal result = revenues.subtract(expenses);
        return new FinancialStatementsResponse(hospital.id(), periodId, Instant.now(), balance.totalDebit(), balance.totalCredit(), money(result),
                List.of(new FinancialStatementSectionResponse("ASSETS", "Actifs", money(assets)),
                        new FinancialStatementSectionResponse("LIABILITIES", "Passifs", money(liabilities)),
                        new FinancialStatementSectionResponse("EQUITY", "Capitaux propres", money(equity)),
                        new FinancialStatementSectionResponse("RESULT", "Résultat de l'exercice", money(result))),
                List.of(new FinancialStatementSectionResponse("REVENUE", "Produits", money(revenues)),
                        new FinancialStatementSectionResponse("EXPENSE", "Charges", money(expenses)),
                        new FinancialStatementSectionResponse("RESULT", "Résultat", money(result))),
                cashFlowStatement(hospital, periodId),
                noteRepository.search(hospital.id(), periodId, "", null, PageRequest.of(0, 1)).getTotalElements());
    }

    /**
     * Lightweight cash-flow view. It classifies a cash/bank/mobile movement by
     * its non-cash counterpart: class 2 assets -> investing; liabilities or
     * equity -> financing; every other operational movement -> operating.
     */
    private List<FinancialStatementSectionResponse> cashFlowStatement(HospitalContext hospital, UUID periodId) {
        Map<UUID, AccountNature> natureByAccount = new HashMap<>();
        for (AccountingAccountEntity account : accountRepository.findAllByHospitalIdOrderByAccountNumberAsc(hospital.id())) {
            natureByAccount.put(account.getId(), account.getNature());
        }
        Map<UUID, List<AccountingEntryLineEntity>> linesByEntry = new LinkedHashMap<>();
        for (AccountingEntryLineEntity line : entryLineRepository.postedLines(hospital.id(), periodId, null, null)) {
            linesByEntry.computeIfAbsent(line.getEntry().getId(), ignored -> new ArrayList<>()).add(line);
        }
        Map<String, BigDecimal> flows = new LinkedHashMap<>();
        flows.put("OPERATING", BigDecimal.ZERO); flows.put("INVESTING", BigDecimal.ZERO); flows.put("FINANCING", BigDecimal.ZERO);
        for (List<AccountingEntryLineEntity> lines : linesByEntry.values()) {
            AccountNature counterpartNature = lines.stream().filter(line -> !isCashAccount(line)).map(line -> natureByAccount.get(line.getAccountId()))
                    .filter(java.util.Objects::nonNull).findFirst().orElse(null);
            boolean hasFixedAsset = lines.stream().filter(line -> !isCashAccount(line)).anyMatch(line -> line.getAccountNumber().startsWith("2"));
            String category = hasFixedAsset ? "INVESTING"
                    : counterpartNature == AccountNature.LIABILITY || counterpartNature == AccountNature.EQUITY ? "FINANCING" : "OPERATING";
            for (AccountingEntryLineEntity line : lines) {
                if (isCashAccount(line)) flows.compute(category, (key, value) -> value.add(line.getDebit()).subtract(line.getCredit()));
            }
        }
        BigDecimal net = flows.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return List.of(new FinancialStatementSectionResponse("OPERATING", "Flux de trésorerie opérationnels", money(flows.get("OPERATING"))),
                new FinancialStatementSectionResponse("INVESTING", "Flux de trésorerie d'investissement", money(flows.get("INVESTING"))),
                new FinancialStatementSectionResponse("FINANCING", "Flux de trésorerie de financement", money(flows.get("FINANCING"))),
                new FinancialStatementSectionResponse("NET_CHANGE", "Variation nette de trésorerie", money(net)));
    }

    private boolean isCashAccount(AccountingEntryLineEntity line) {
        return line.getAccountNumber().startsWith("5") || ACCOUNT_CASH.equals(line.getAccountNumber())
                || ACCOUNT_BANK.equals(line.getAccountNumber()) || ACCOUNT_MOBILE_MONEY.equals(line.getAccountNumber());
    }

    public AccountingDashboardResponse dashboard(UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        return new AccountingDashboardResponse(hospital.id(), hospital.code(),
                invoiceRepository.countByHospitalIdAndStatusIn(hospital.id(), List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID)),
                money(invoiceRepository.sumOutstandingByHospitalId(hospital.id())),
                periodRepository.search(hospital.id(), "", AccountingPeriodStatus.OPEN, PageRequest.of(0, 1)).getTotalElements(),
                entryRepository.countByHospitalIdAndStatus(hospital.id(), AccountingEntryStatus.POSTED)
                        + entryRepository.countByHospitalIdAndStatus(hospital.id(), AccountingEntryStatus.REVERSED),
                noteRepository.countByHospitalIdAndStatus(hospital.id(), FinancialStatementNoteStatus.DRAFT));
    }

    public PageResponse<FinancialStatementNoteResponse> searchNotes(int page, int size, String query, FinancialStatementNoteStatus status,
            UUID periodId, UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        return page(noteRepository.search(hospital.id(), periodId, normalize(query), status, pageable(page, size, "createdAt")).map(this::toNote));
    }

    @Transactional
    public FinancialStatementNoteResponse createNote(CreateFinancialStatementNoteRequest request, DataAccessScope scope, AuditActor actor) {
        HospitalContext hospital = hospital(scope, request.hospitalId()); ensureHospitalBook(hospital);
        if (request.periodId() != null) requirePeriodForHospital(request.periodId(), hospital);
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (noteRepository.existsByHospitalIdAndCode(hospital.id(), code)) throw new AccountingValidationException("Ce code d'annexe existe déjà.");
        return toNote(noteRepository.save(new FinancialStatementNoteEntity(UUID.randomUUID(), hospital.id(), hospital.code(), request.periodId(), code,
                request.title().trim(), request.type(), request.content().trim(), actor.userId(), actor.username(), Instant.now())));
    }

    @Transactional
    public FinancialStatementNoteResponse updateNote(UUID noteId, UpdateFinancialStatementNoteRequest request, DataAccessScope scope) {
        FinancialStatementNoteEntity note = ownedNote(noteId, scope);
        note.update(request.title().trim(), request.type(), request.content().trim());
        return toNote(note);
    }

    @Transactional
    public FinancialStatementNoteResponse validateNote(UUID noteId, DataAccessScope scope, AuditActor actor) {
        FinancialStatementNoteEntity note = ownedNote(noteId, scope);
        if (note.getStatus() == FinancialStatementNoteStatus.VALIDATED) return toNote(note);
        note.validate(actor.userId(), actor.username(), Instant.now());
        return toNote(note);
    }

    public List<AccountingSupportingDocumentResponse> listDocuments(String relatedType, UUID relatedId, DataAccessScope scope) {
        HospitalContext hospital = hospitalForRelatedResource(relatedType, relatedId, scope);
        return documentRepository.findAllByHospitalIdAndRelatedTypeAndRelatedIdOrderByUploadedAtDesc(hospital.id(), relatedType, relatedId)
                .stream().map(this::toDocument).toList();
    }

    @Transactional
    public AccountingSupportingDocumentResponse uploadDocument(String relatedType, UUID relatedId,
            UploadAccountingSupportingDocumentRequest request, DataAccessScope scope, AuditActor actor) {
        HospitalContext hospital = hospitalForRelatedResource(relatedType, relatedId, scope);
        byte[] bytes;
        try { bytes = java.util.Base64.getMimeDecoder().decode(request.contentBase64()); }
        catch (IllegalArgumentException exception) { throw new AccountingValidationException("Le contenu du justificatif n'est pas un base64 valide."); }
        if (bytes.length == 0 || bytes.length > 3 * 1024 * 1024) throw new AccountingValidationException("Le justificatif doit peser entre 1 octet et 3 Mo.");
        return toDocument(documentRepository.save(new AccountingSupportingDocumentEntity(UUID.randomUUID(), hospital.id(), hospital.code(),
                relatedType, relatedId, request.type(), request.fileName().trim(), request.contentType().trim(), request.contentBase64(),
                bytes.length, actor.userId(), actor.username(), Instant.now())));
    }

    public AccountingSupportingDocumentEntity getDocument(UUID documentId, DataAccessScope scope) {
        AccountingSupportingDocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new AccountingResourceNotFoundException("Le justificatif"));
        if (!scope.administrator()) {
            HospitalContext hospital = hospital(scope, null);
            if (!document.getHospitalId().equals(hospital.id())) throw new AccountingAccessDeniedException();
        }
        return document;
    }

    public PageResponse<CashSessionResponse> searchCashSessions(int page, int size, String query, CashSessionStatus status,
            UUID requestedHospitalId, DataAccessScope scope) {
        HospitalContext hospital = hospital(scope, requestedHospitalId); ensureHospitalBook(hospital);
        return page(cashSessionRepository.search(hospital.id(), normalize(query), status, pageable(page, size, "openedAt"))
                .map(this::toCashSession));
    }

    @Transactional
    public CashSessionResponse openCashSession(OpenCashSessionRequest request, DataAccessScope scope, AuditActor actor) {
        HospitalContext hospital = hospital(scope, request.hospitalId()); ensureHospitalBook(hospital);
        if (cashSessionRepository.findByHospitalIdAndCurrencyAndStatus(hospital.id(), request.currency(), CashSessionStatus.OPEN).isPresent()) {
            throw new AccountingValidationException("Une caisse est déjà ouverte pour cette devise dans cet hôpital.");
        }
        String code = nextCode("CAI", value -> cashSessionRepository.existsByHospitalIdAndCode(hospital.id(), value));
        return toCashSession(cashSessionRepository.save(new CashSessionEntity(UUID.randomUUID(), hospital.id(), hospital.code(), code,
                request.currency(), money(request.openingAmount()), actor.userId(), actor.username(), Instant.now())));
    }

    @Transactional
    public CashSessionResponse closeCashSession(UUID cashSessionId, CloseCashSessionRequest request, DataAccessScope scope, AuditActor actor) {
        CashSessionEntity session = cashSessionRepository.findById(cashSessionId)
                .orElseThrow(() -> new AccountingResourceNotFoundException("La caisse"));
        // Like invoices, periods and vouchers, a central administrator may act
        // on a selected hospital resource even without being personally
        // attached to that hospital. Non-administrators remain confined to
        // their own hospital through assertOwned.
        assertOwned(session.getHospitalId(), scope);
        if (session.getStatus() != CashSessionStatus.OPEN) throw new AccountingValidationException("Cette caisse est déjà clôturée.");
        Instant now = Instant.now();
        BigDecimal receipts = paymentRepository.sumReceived(session.getHospitalId(), session.getCurrency(), AccountingPaymentMethod.CASH,
                session.getOpenedAt(), now);
        BigDecimal expected = session.getOpeningAmount().add(receipts == null ? BigDecimal.ZERO : receipts);
        session.close(money(expected), money(request.declaredClosingAmount()), trimToNull(request.notes()), actor.userId(), actor.username(), now);
        return toCashSession(session);
    }

    /**
     * Consumes only a dispensing code and rebuilds accounting data from source
     * services. The invoice key makes retries from the patient outbox harmless.
     * One delivery creates distinct sales, receipt and inventory-cost vouchers.
     */
    @Transactional
    public PharmacyDispensationAccountingResponse recordPharmacyDispensation(String rawDispenseCode) {
        String dispenseCode = rawDispenseCode.trim().toUpperCase(Locale.ROOT);
        PatientAccountingReferenceClient.PharmacyDispenseReference patient = patientReferenceClient.resolve(dispenseCode);
        PharmacyAccountingReferenceClient.PharmacyDispenseStockReference pharmacy = pharmacyReferenceClient.resolve(dispenseCode);
        if (patient.hospitalId() == null || pharmacy.hospitalId() == null || !patient.hospitalId().equals(pharmacy.hospitalId())) {
            throw new AccountingValidationException("Les références patient et pharmacie de la délivrance ne correspondent pas.");
        }
        HospitalReferenceClient.HospitalReference reference = hospitalReferenceClient.resolveActive(patient.hospitalId());
        HospitalContext hospital = new HospitalContext(reference.hospitalId(), reference.hospitalCode()); ensureHospitalBook(hospital);
        var existing = invoiceRepository.findByHospitalIdAndSourceTypeAndSourceCode(hospital.id(), AccountingSourceType.PHARMACY_DISPENSE, dispenseCode);
        if (existing.isPresent()) {
            AccountingInvoiceEntity invoice = existing.get();
            pharmacyPaymentSettlementOutboxService.enqueueInvoiceState(invoice);
            AccountingEntryEntity sales = entryRepository.findByHospitalIdAndSourceTypeAndSourceCode(hospital.id(),
                    AccountingSourceType.PHARMACY_DISPENSE, dispenseCode + ":INVOICE").orElse(null);
            return new PharmacyDispensationAccountingResponse(invoice.getId(), invoice.getCode(), invoice.getCode(),
                    sales == null ? null : sales.getId(), sales == null ? null : sales.getCode(), invoice.getStatus(), true);
        }
        AccountingCurrency currency = parseCurrency(patient.currency());
        if (pharmacy.currency() != null && parseCurrency(pharmacy.currency()) != currency) {
            throw new AccountingValidationException("Les devises de la délivrance et du coût de stock divergent.");
        }
        BigDecimal total = money(patient.totalAmount() == null ? patient.paidAmount() : patient.totalAmount());
        BigDecimal paid = money(patient.paidAmount());
        BigDecimal stockCost = money(pharmacy.totalCost());
        if (total.signum() < 0 || paid.signum() < 0 || stockCost.signum() < 0 || paid.compareTo(total) > 0) {
            throw new AccountingValidationException("Les montants de la délivrance sont incohérents.");
        }
        AuditActor actor = new AuditActor(orSystem(patient.dispensedByUserId()), orSystem(patient.dispensedByUsername()));
        LocalDate date = patient.dispensedAt() == null ? LocalDate.now() : patient.dispensedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        String invoiceCode = nextCode("FAC", value -> invoiceRepository.existsByHospitalIdAndCode(hospital.id(), value));
        AccountingInvoiceEntity invoice = invoiceRepository.save(new AccountingInvoiceEntity(UUID.randomUUID(), hospital.id(), hospital.code(),
                invoiceCode, AccountingSourceType.PHARMACY_DISPENSE, dispenseCode, patient.patientId(), patient.patientCode(),
                patient.passageId(), patient.passageCode(), date, currency, total, "Délivrance pharmacie " + dispenseCode,
                actor.userId(), actor.username(), Instant.now()));
        invoice.issue();
        pharmacyPaymentSettlementOutboxService.enqueueInvoiceState(invoice);
        AccountingEntryEntity sales = null;
        if (total.signum() > 0) {
            sales = createAndPostEntry(hospital, date, journal(hospital, "VEN"), AccountingSourceType.PHARMACY_DISPENSE,
                    dispenseCode + ":INVOICE", "Vente pharmacie " + dispenseCode, currency, actor,
                    List.of(new LineSpec(account(hospital, ACCOUNT_RECEIVABLES), "Créance patient " + dispenseCode, total, BigDecimal.ZERO, patient.patientCode()),
                            new LineSpec(account(hospital, ACCOUNT_PHARMACY_REVENUE), "Vente pharmacie " + dispenseCode, BigDecimal.ZERO, total, patient.patientCode())));
        }
        if (paid.signum() > 0) {
            recordPayment(hospital, invoice, date, paid, currency, parsePaymentMethod(patient.paymentMethod()),
                    dispenseCode, null, actor, AccountingSourceType.PHARMACY_DISPENSE, dispenseCode + ":PAYMENT");
        }
        if (stockCost.signum() > 0) {
            createAndPostEntry(hospital, date, journal(hospital, "OD"), AccountingSourceType.PHARMACY_DISPENSE,
                    dispenseCode + ":COGS", "Coût des médicaments délivrés " + dispenseCode, currency, actor,
                    List.of(new LineSpec(account(hospital, ACCOUNT_MEDICINE_COST), "Coût de délivrance " + dispenseCode, stockCost, BigDecimal.ZERO, patient.patientCode()),
                            new LineSpec(account(hospital, ACCOUNT_MEDICINE_STOCK), "Sortie de stock " + dispenseCode, BigDecimal.ZERO, stockCost, patient.patientCode())));
        }
        return new PharmacyDispensationAccountingResponse(invoice.getId(), invoice.getCode(), invoice.getCode(),
                sales == null ? null : sales.getId(), sales == null ? null : sales.getCode(), invoice.getStatus(), false);
    }

    /**
     * Records the purchase-side accounting of an already committed pharmacy
     * stock reception. The only input is its immutable business code; all
     * value and supplier information is reread from pharmacy-service.
     */
    @Transactional
    public PharmacyStockReceiptAccountingResponse recordPharmacyStockReceipt(String rawStockEntryCode) {
        String stockEntryCode = rawStockEntryCode == null ? "" : rawStockEntryCode.trim().toUpperCase(Locale.ROOT);
        if (stockEntryCode.isBlank()) {
            throw new AccountingValidationException("Le code de l'entrée de stock est obligatoire.");
        }
        PharmacyAccountingReferenceClient.PharmacyStockEntryReference source = pharmacyReferenceClient.resolveStockEntry(stockEntryCode);
        if (source.hospitalId() == null || source.totalCost() == null || source.currency() == null) {
            throw new AccountingValidationException("La référence comptable de l'entrée de stock est incomplète.");
        }
        HospitalReferenceClient.HospitalReference reference = hospitalReferenceClient.resolveActive(source.hospitalId());
        HospitalContext hospital = new HospitalContext(reference.hospitalId(), reference.hospitalCode());
        ensureHospitalBook(hospital);
        String canonicalCode = source.stockEntryCode() == null || source.stockEntryCode().isBlank()
                ? stockEntryCode
                : source.stockEntryCode().trim().toUpperCase(Locale.ROOT);
        AccountingEntryEntity existing = entryRepository.findByHospitalIdAndSourceTypeAndSourceCode(
                hospital.id(), AccountingSourceType.PHARMACY_STOCK_RECEIPT, canonicalCode).orElse(null);
        if (existing != null) {
            return new PharmacyStockReceiptAccountingResponse(existing.getId(), existing.getCode(), existing.getCode(), true);
        }
        BigDecimal totalCost = money(source.totalCost());
        if (totalCost.signum() <= 0) {
            throw new AccountingValidationException("Le montant d'une entrée de stock doit être strictement positif.");
        }
        AccountingCurrency currency = parseCurrency(source.currency());
        LocalDate date = source.receivedAt() == null
                ? LocalDate.now()
                : source.receivedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        AuditActor actor = new AuditActor(orSystem(source.receivedByUserId()), orSystem(source.receivedByUsername()));
        String supplierName = trimToNull(source.supplierName());
        AccountingAccountEntity creditAccount = supplierName == null
                ? account(hospital, ACCOUNT_STOCK_RECEIPT_CLEARING)
                : account(hospital, "401100");
        String counterparty = supplierName == null ? canonicalCode : supplierName;
        AccountingEntryEntity entry = createAndPostEntry(
                hospital,
                date,
                journal(hospital, "ACH"),
                AccountingSourceType.PHARMACY_STOCK_RECEIPT,
                canonicalCode,
                "Réception de stock pharmacie " + canonicalCode,
                currency,
                actor,
                List.of(
                        new LineSpec(account(hospital, ACCOUNT_MEDICINE_STOCK),
                                "Entrée de stock " + canonicalCode, totalCost, BigDecimal.ZERO, counterparty),
                        new LineSpec(creditAccount,
                                supplierName == null
                                        ? "Réception à rapprocher " + canonicalCode
                                        : "Fournisseur " + supplierName,
                                BigDecimal.ZERO, totalCost, counterparty)));
        return new PharmacyStockReceiptAccountingResponse(entry.getId(), entry.getCode(), entry.getCode(), false);
    }

    /**
     * Records non-prescription pharmacy stock-outs. The source movement is
     * immutable and the voucher source key makes every retry idempotent.
     *
     * <p>A patient prescription delivery is intentionally ignored here: its
     * invoice, receipt and cost voucher are already created by the patient
     * dispense outbox. A manual dispensing has no reliable patient, price or
     * payment context, so it is posted to an explicit clearing account rather
     * than being incorrectly recognised as revenue.</p>
     */
    @Transactional
    public PharmacyStockMovementAccountingResponse recordPharmacyStockMovement(String rawStockMovementCode) {
        String stockMovementCode = rawStockMovementCode == null
                ? ""
                : rawStockMovementCode.trim().toUpperCase(Locale.ROOT);
        if (stockMovementCode.isBlank()) {
            throw new AccountingValidationException("Le code du mouvement de stock est obligatoire.");
        }
        PharmacyAccountingReferenceClient.PharmacyStockMovementReference source = pharmacyReferenceClient
                .resolveStockMovement(stockMovementCode);
        String canonicalCode = source.stockMovementCode() == null || source.stockMovementCode().isBlank()
                ? stockMovementCode
                : source.stockMovementCode().trim().toUpperCase(Locale.ROOT);
        String movementType = upper(source.type());
        if (isPrescriptionDispenseSource(source)) {
            return new PharmacyStockMovementAccountingResponse(
                    null,
                    null,
                    null,
                    false,
                    true,
                    "Cette délivrance patient est déjà comptabilisée par le flux DSP.");
        }
        if ("ENTRY".equals(movementType)) {
            return new PharmacyStockMovementAccountingResponse(
                    null,
                    null,
                    null,
                    false,
                    true,
                    "Cette entrée est comptabilisée par le flux des réceptions de stock.");
        }
        if (!List.of("LOSS", "EXPIRY", "TRANSFER_OUT", "DISPENSING").contains(movementType)) {
            throw new AccountingValidationException("Le type de sortie de stock est indisponible pour le rapprochement comptable.");
        }
        if (source.hospitalId() == null || source.totalCost() == null || source.currency() == null
                || source.quantity() <= 0) {
            throw new AccountingValidationException("La référence comptable de la sortie de stock est incomplète.");
        }
        HospitalReferenceClient.HospitalReference reference = hospitalReferenceClient.resolveActive(source.hospitalId());
        HospitalContext hospital = new HospitalContext(reference.hospitalId(), reference.hospitalCode());
        ensureHospitalBook(hospital);
        AccountingEntryEntity existing = entryRepository.findByHospitalIdAndSourceTypeAndSourceCode(
                hospital.id(), AccountingSourceType.PHARMACY_STOCK_MOVEMENT, canonicalCode).orElse(null);
        if (existing != null) {
            return new PharmacyStockMovementAccountingResponse(
                    existing.getId(), existing.getCode(), existing.getCode(), true, false, null);
        }
        BigDecimal totalCost = money(source.totalCost());
        if (totalCost.signum() == 0) {
            return new PharmacyStockMovementAccountingResponse(
                    null,
                    null,
                    null,
                    false,
                    true,
                    "La valeur comptable de cette sortie est nulle : aucune écriture n'est générée.");
        }
        if (totalCost.signum() < 0) {
            throw new AccountingValidationException("La valeur de la sortie de stock ne peut pas être négative.");
        }
        AccountingCurrency currency = parseCurrency(source.currency());
        LocalDate date = source.occurredAt() == null
                ? LocalDate.now()
                : source.occurredAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
        AuditActor actor = new AuditActor(orSystem(source.performedByUserId()), orSystem(source.performedByUsername()));
        StockMovementPosting posting = stockMovementPosting(hospital, movementType, canonicalCode);
        String thirdPartyReference = trimToNull(source.sourceCode()) == null
                ? canonicalCode
                : source.sourceCode().trim();
        AccountingEntryEntity entry = createAndPostEntry(
                hospital,
                date,
                journal(hospital, "OD"),
                AccountingSourceType.PHARMACY_STOCK_MOVEMENT,
                canonicalCode,
                posting.description(),
                currency,
                actor,
                List.of(
                        new LineSpec(posting.debitAccount(), posting.debitLabel(), totalCost, BigDecimal.ZERO,
                                thirdPartyReference),
                        new LineSpec(account(hospital, ACCOUNT_MEDICINE_STOCK),
                                "Sortie de stock " + canonicalCode,
                                BigDecimal.ZERO,
                                totalCost,
                                thirdPartyReference)));
        return new PharmacyStockMovementAccountingResponse(
                entry.getId(), entry.getCode(), entry.getCode(), false, false, null);
    }

    private AccountingPaymentEntity recordPayment(HospitalContext hospital, AccountingInvoiceEntity invoice, LocalDate paidOn,
            BigDecimal amount, AccountingCurrency currency, AccountingPaymentMethod method, String paymentReference,
            String idempotencyKey, AuditActor actor, AccountingSourceType sourceType, String sourceCode) {
        if (amount.signum() <= 0) throw new AccountingValidationException("Un paiement doit être supérieur à zéro.");
        if (invoice.getCurrency() != currency) throw new AccountingValidationException("La devise du paiement est différente de la facture.");
        if (invoice.getDueAmount().compareTo(amount) < 0) throw new AccountingValidationException("Le paiement dépasse le solde de la facture.");
        AccountingJournalEntity journal = journal(hospital, method == AccountingPaymentMethod.CASH ? "CAI" : "BNQ");
        AccountingAccountEntity settlementAccount = switch (method) {
            case CASH -> account(hospital, ACCOUNT_CASH);
            case MOBILE_MONEY -> account(hospital, ACCOUNT_MOBILE_MONEY);
            default -> account(hospital, ACCOUNT_BANK);
        };
        AccountingEntryEntity entry = createAndPostEntry(hospital, paidOn, journal, sourceType, sourceCode,
                "Encaissement de la facture " + invoice.getCode(), currency, actor,
                List.of(new LineSpec(settlementAccount, "Encaissement " + invoice.getCode(), amount, BigDecimal.ZERO, invoice.getPatientCode()),
                        new LineSpec(account(hospital, ACCOUNT_RECEIVABLES), "Règlement " + invoice.getCode(), BigDecimal.ZERO, amount, invoice.getPatientCode())));
        invoice.receive(amount);
        String paymentCode = nextCode("REC", value -> paymentRepository.existsByHospitalIdAndCode(hospital.id(), value));
        AccountingPaymentEntity payment = paymentRepository.save(new AccountingPaymentEntity(UUID.randomUUID(), hospital.id(), hospital.code(), paymentCode,
                invoice, paidOn, amount, currency, method, paymentReference, idempotencyKey, entry, actor.userId(), actor.username(), Instant.now()));
        pharmacyPaymentSettlementOutboxService.enqueuePaymentState(payment, invoice);
        return payment;
    }

    private AccountingEntryEntity createAndPostEntry(HospitalContext hospital, LocalDate date, AccountingJournalEntity journal,
            AccountingSourceType sourceType, String sourceCode, String description, AccountingCurrency currency, AuditActor actor,
            List<LineSpec> lines) {
        AccountingPeriodEntity period = openPeriod(hospital, date);
        BigDecimal debit = lines.stream().map(LineSpec::debit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credit = lines.stream().map(LineSpec::credit).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertBalanced(debit, credit);
        String code = nextCode("ECR", value -> entryRepository.existsByHospitalIdAndCode(hospital.id(), value));
        AccountingEntryEntity entry = entryRepository.save(new AccountingEntryEntity(UUID.randomUUID(), hospital.id(), hospital.code(),
                period.getId(), journal.getId(), journal.getCode(), code, sourceType, sourceCode, date, description, currency,
                money(debit), money(credit), actor.userId(), actor.username(), Instant.now()));
        List<AccountingEntryLineEntity> persisted = new ArrayList<>(); int number = 1;
        for (LineSpec line : lines) persisted.add(new AccountingEntryLineEntity(UUID.randomUUID(), entry, number++, line.account(),
                line.label(), money(line.debit()), money(line.credit()), trimToNull(line.thirdPartyReference())));
        entryLineRepository.saveAll(persisted);
        entry.post(actor.userId(), actor.username(), Instant.now());
        return entry;
    }

    private List<AccountingEntryLineEntity> saveLines(AccountingEntryEntity entry, List<ResolvedLine> lines) {
        List<AccountingEntryLineEntity> saved = new ArrayList<>(); int number = 1;
        for (ResolvedLine line : lines) saved.add(new AccountingEntryLineEntity(UUID.randomUUID(), entry, number++, line.account(),
                line.label(), line.debit(), line.credit(), line.thirdPartyReference()));
        return entryLineRepository.saveAll(saved);
    }

    private List<ResolvedLine> resolveAndValidateLines(HospitalContext hospital, List<CreateAccountingEntryLineRequest> requests) {
        List<ResolvedLine> lines = new ArrayList<>();
        for (CreateAccountingEntryLineRequest request : requests) {
            AccountingAccountEntity account = accountRepository.findById(request.accountId())
                    .filter(item -> item.getHospitalId().equals(hospital.id()) && item.isActive())
                    .orElseThrow(() -> new AccountingValidationException("Un compte sélectionné est indisponible dans cet hôpital."));
            BigDecimal debit = money(request.debit()); BigDecimal credit = money(request.credit());
            if ((debit.signum() > 0) == (credit.signum() > 0)) {
                throw new AccountingValidationException("Chaque ligne doit comporter un débit ou un crédit strictement positif.");
            }
            lines.add(new ResolvedLine(account, trimToNull(request.label()) == null ? account.getLabel() : request.label().trim(),
                    debit, credit, trimToNull(request.thirdPartyReference())));
        }
        return lines;
    }

    private void assertBalanced(BigDecimal debit, BigDecimal credit) {
        if (debit.signum() <= 0 || debit.compareTo(credit) != 0) {
            throw new AccountingValidationException("Une écriture doit être équilibrée et comporter un montant strictement positif.");
        }
    }

    private HospitalContext hospital(DataAccessScope scope, UUID requestedHospitalId) {
        // A province-wide scope alone never opens all hospital books. Only the
        // central administrator may select an arbitrary hospital accounting.
        if (scope.administrator()) {
            UUID hospitalId = requestedHospitalId == null ? scope.hospitalId() : requestedHospitalId;
            if (hospitalId == null) throw new AccountingAccessDeniedException();
            HospitalReferenceClient.HospitalReference reference = hospitalReferenceClient.resolveActive(hospitalId);
            return new HospitalContext(reference.hospitalId(), reference.hospitalCode());
        }
        if (scope.hospitalId() == null || (requestedHospitalId != null && !scope.hospitalId().equals(requestedHospitalId))) {
            throw new AccountingAccessDeniedException();
        }
        if (scope.hospitalCode() == null || scope.hospitalCode().isBlank()) {
            HospitalReferenceClient.HospitalReference reference = hospitalReferenceClient.resolveActive(scope.hospitalId());
            return new HospitalContext(reference.hospitalId(), reference.hospitalCode());
        }
        return new HospitalContext(scope.hospitalId(), scope.hospitalCode());
    }

    private DataAccessScope scopeForHospital(HospitalContext hospital) {
        return new DataAccessScope(true, true, hospital.id(), hospital.code());
    }

    private void ensureHospitalBook(HospitalContext hospital) {
        Instant now = Instant.now();
        List<AccountingAccountEntity> existingAccounts = accountRepository.findAllByHospitalIdOrderByAccountNumberAsc(hospital.id());
        if (existingAccounts.isEmpty()) {
            accountRepository.saveAll(defaultAccounts(hospital, now));
        } else {
            java.util.Set<String> existingNumbers = existingAccounts.stream()
                    .map(AccountingAccountEntity::getAccountNumber)
                    .collect(java.util.stream.Collectors.toSet());
            List<AccountingAccountEntity> missingAccounts = defaultAccounts(hospital, now).stream()
                    .filter(defaultAccount -> !existingNumbers.contains(defaultAccount.getAccountNumber()))
                    .toList();
            if (!missingAccounts.isEmpty()) {
                accountRepository.saveAll(missingAccounts);
            }
        }
        if (journalRepository.findAllByHospitalIdOrderByCodeAsc(hospital.id()).isEmpty()) {
            journalRepository.saveAll(defaultJournals(hospital, now));
        }
        // The first operation (including an asynchronous pharmacy event) must
        // never wait for an accountant to create an exercise manually. A
        // closed period must not however make read-only screens unusable.
        // Creating a fallback period is safe only when no existing period
        // covers today.
        LocalDate today = LocalDate.now();
        if (!periodRepository.existsByHospitalIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(hospital.id(), today, today)) {
            openPeriod(hospital, today);
        }
    }

    private List<AccountingAccountEntity> defaultAccounts(HospitalContext hospital, Instant at) {
        return List.of(
                defaultAccount(hospital, "101000", "Capital et dotations", AccountNature.EQUITY, at),
                defaultAccount(hospital, "218000", "Matériel et équipements", AccountNature.ASSET, at),
                defaultAccount(hospital, ACCOUNT_MEDICINE_STOCK, "Stock de médicaments", AccountNature.ASSET, at),
                defaultAccount(hospital, ACCOUNT_RECEIVABLES, "Patients et tiers débiteurs", AccountNature.ASSET, at),
                defaultAccount(hospital, "401100", "Fournisseurs", AccountNature.LIABILITY, at),
                defaultAccount(hospital, ACCOUNT_STOCK_RECEIPT_CLEARING, "Réceptions de stock à rapprocher", AccountNature.LIABILITY, at),
                defaultAccount(hospital, ACCOUNT_BANK, "Banques", AccountNature.ASSET, at),
                defaultAccount(hospital, ACCOUNT_MOBILE_MONEY, "Encaissements mobile money", AccountNature.ASSET, at),
                defaultAccount(hospital, ACCOUNT_CASH, "Caisse", AccountNature.ASSET, at),
                defaultAccount(hospital, ACCOUNT_MEDICINE_COST, "Coût des médicaments délivrés", AccountNature.EXPENSE, at),
                defaultAccount(hospital, ACCOUNT_STOCK_TRANSFER_CLEARING, "Stocks transférés à rapprocher", AccountNature.ASSET, at),
                defaultAccount(hospital, ACCOUNT_MANUAL_DISPENSE_CLEARING, "Délivrances pharmacie à rapprocher", AccountNature.ASSET, at),
                defaultAccount(hospital, ACCOUNT_MEDICINE_LOSS, "Pertes et avaries de stock", AccountNature.EXPENSE, at),
                defaultAccount(hospital, ACCOUNT_MEDICINE_EXPIRY, "Pertes sur péremption de stock", AccountNature.EXPENSE, at),
                defaultAccount(hospital, ACCOUNT_CARE_REVENUE, "Prestations hospitalières", AccountNature.REVENUE, at),
                defaultAccount(hospital, ACCOUNT_PHARMACY_REVENUE, "Ventes de médicaments", AccountNature.REVENUE, at));
    }

    private AccountingAccountEntity defaultAccount(HospitalContext hospital, String number, String label, AccountNature nature, Instant at) {
        return new AccountingAccountEntity(UUID.randomUUID(), hospital.id(), hospital.code(), number, label, number.substring(0, 1), nature, true, at);
    }

    private List<AccountingJournalEntity> defaultJournals(HospitalContext hospital, Instant at) {
        return List.of(
                defaultJournal(hospital, "VEN", "Journal des ventes", JournalType.SALES, at),
                defaultJournal(hospital, "ACH", "Journal des achats", JournalType.PURCHASES, at),
                defaultJournal(hospital, "CAI", "Journal de caisse", JournalType.CASH, at),
                defaultJournal(hospital, "BNQ", "Journal de banque", JournalType.BANK, at),
                defaultJournal(hospital, "OD", "Opérations diverses", JournalType.GENERAL, at),
                defaultJournal(hospital, "OUV", "Journal d'ouverture", JournalType.OPENING, at));
    }

    private AccountingJournalEntity defaultJournal(HospitalContext hospital, String code, String label, JournalType type, Instant at) {
        return new AccountingJournalEntity(UUID.randomUUID(), hospital.id(), hospital.code(), code, label, type, true, at);
    }

    private AccountingPeriodEntity openPeriod(HospitalContext hospital, LocalDate date) {
        return periodRepository.findOpenContaining(hospital.id(), date).orElseGet(() -> {
            // A closed custom period may not carry the default EX-YYYY code.
            // It still prevents a new posting for its dates and must never be
            // silently overlapped by an automatically-created annual period.
            if (periodRepository.existsByHospitalIdAndStartsOnLessThanEqualAndEndsOnGreaterThanEqual(hospital.id(), date, date)) {
                throw new AccountingValidationException("La période comptable couvrant cette date est clôturée.");
            }
            String code = "EX-" + date.getYear();
            AccountingPeriodEntity existing = periodRepository.findByHospitalIdAndCode(hospital.id(), code).orElse(null);
            if (existing != null) {
                if (existing.getStatus() == AccountingPeriodStatus.CLOSED) throw new AccountingValidationException("La période " + code + " est clôturée.");
                return existing;
            }
            return periodRepository.save(new AccountingPeriodEntity(UUID.randomUUID(), hospital.id(), hospital.code(), code,
                    "Exercice " + date.getYear(), LocalDate.of(date.getYear(), 1, 1), LocalDate.of(date.getYear(), 12, 31), Instant.now()));
        });
    }

    private AccountingAccountEntity account(HospitalContext hospital, String number) {
        return accountRepository.findByHospitalIdAndAccountNumber(hospital.id(), number)
                .filter(AccountingAccountEntity::isActive)
                .orElseThrow(() -> new AccountingValidationException("Le compte " + number + " requis par l'automatisation est indisponible."));
    }

    private StockMovementPosting stockMovementPosting(
            HospitalContext hospital,
            String movementType,
            String movementCode) {
        return switch (movementType) {
            case "LOSS" -> new StockMovementPosting(
                    account(hospital, ACCOUNT_MEDICINE_LOSS),
                    "Perte de stock " + movementCode,
                    "Constat de perte de stock pharmacie " + movementCode);
            case "EXPIRY" -> new StockMovementPosting(
                    account(hospital, ACCOUNT_MEDICINE_EXPIRY),
                    "Péremption de stock " + movementCode,
                    "Constat de péremption de stock pharmacie " + movementCode);
            case "TRANSFER_OUT" -> new StockMovementPosting(
                    account(hospital, ACCOUNT_STOCK_TRANSFER_CLEARING),
                    "Stock transféré à rapprocher " + movementCode,
                    "Transfert sortant de stock pharmacie " + movementCode);
            case "DISPENSING" -> new StockMovementPosting(
                    account(hospital, ACCOUNT_MANUAL_DISPENSE_CLEARING),
                    "Délivrance à rapprocher " + movementCode,
                    "Délivrance manuelle de pharmacie à rapprocher " + movementCode);
            default -> throw new AccountingValidationException(
                    "Le type de sortie de stock est indisponible pour le rapprochement comptable.");
        };
    }

    private boolean isPrescriptionDispenseSource(PharmacyAccountingReferenceClient.PharmacyStockMovementReference source) {
        if ("PRESCRIPTION_DISPENSE".equals(upper(source.sourceType()))) {
            return true;
        }
        // Historical rows can predate source_type. A DSP business reference is
        // still authoritative and must never be duplicated by this flow.
        String sourceCode = upper(source.sourceCode());
        return "DISPENSING".equals(upper(source.type())) && sourceCode.startsWith("DSP-");
    }

    private AccountingJournalEntity journal(HospitalContext hospital, String code) {
        return journalRepository.findByHospitalIdAndCode(hospital.id(), code)
                .filter(AccountingJournalEntity::isActive)
                .orElseThrow(() -> new AccountingValidationException("Le journal " + code + " requis par l'automatisation est indisponible."));
    }

    private HospitalContext hospitalForRelatedResource(String relatedType, UUID relatedId, DataAccessScope scope) {
        String type = relatedType == null ? "" : relatedType.trim().toUpperCase(Locale.ROOT);
        UUID hospitalId = switch (type) {
            case "INVOICE" -> invoiceRepository.findById(relatedId).orElseThrow(() -> new AccountingResourceNotFoundException("La facture")).getHospitalId();
            case "PAYMENT" -> paymentRepository.findById(relatedId).orElseThrow(() -> new AccountingResourceNotFoundException("Le paiement")).getHospitalId();
            case "ENTRY" -> entryRepository.findById(relatedId).orElseThrow(() -> new AccountingResourceNotFoundException("L'écriture")).getHospitalId();
            case "NOTE" -> noteRepository.findById(relatedId).orElseThrow(() -> new AccountingResourceNotFoundException("L'annexe")).getHospitalId();
            default -> throw new AccountingValidationException("Le type de justificatif est invalide.");
        };
        if (scope.administrator()) {
            HospitalReferenceClient.HospitalReference reference = hospitalReferenceClient.resolveActive(hospitalId);
            return new HospitalContext(reference.hospitalId(), reference.hospitalCode());
        }
        HospitalContext hospital = hospital(scope, null);
        if (!hospital.id().equals(hospitalId)) throw new AccountingAccessDeniedException();
        return hospital;
    }

    private AccountingAccountEntity ownedAccount(UUID id, DataAccessScope scope) {
        AccountingAccountEntity entity = accountRepository.findById(id).orElseThrow(() -> new AccountingResourceNotFoundException("Le compte"));
        assertOwned(entity.getHospitalId(), scope); return entity;
    }
    private AccountingJournalEntity ownedJournal(UUID id, DataAccessScope scope) {
        AccountingJournalEntity entity = journalRepository.findById(id).orElseThrow(() -> new AccountingResourceNotFoundException("Le journal"));
        assertOwned(entity.getHospitalId(), scope); return entity;
    }
    private AccountingPeriodEntity ownedPeriod(UUID id, DataAccessScope scope) {
        AccountingPeriodEntity entity = periodRepository.findById(id).orElseThrow(() -> new AccountingResourceNotFoundException("La période"));
        assertOwned(entity.getHospitalId(), scope); return entity;
    }
    private AccountingEntryEntity ownedEntry(UUID id, DataAccessScope scope) {
        AccountingEntryEntity entity = entryRepository.findById(id).orElseThrow(() -> new AccountingResourceNotFoundException("L'écriture"));
        assertOwned(entity.getHospitalId(), scope); return entity;
    }
    private AccountingInvoiceEntity ownedInvoice(UUID id, DataAccessScope scope) {
        AccountingInvoiceEntity entity = invoiceRepository.findById(id).orElseThrow(() -> new AccountingResourceNotFoundException("La facture"));
        assertOwned(entity.getHospitalId(), scope); return entity;
    }
    private FinancialStatementNoteEntity ownedNote(UUID id, DataAccessScope scope) {
        FinancialStatementNoteEntity entity = noteRepository.findById(id).orElseThrow(() -> new AccountingResourceNotFoundException("L'annexe"));
        assertOwned(entity.getHospitalId(), scope); return entity;
    }
    private void requirePeriodForHospital(UUID periodId, HospitalContext hospital) {
        if (periodRepository.findById(periodId).filter(item -> item.getHospitalId().equals(hospital.id())).isEmpty()) {
            throw new AccountingValidationException("La période sélectionnée ne relève pas de cet hôpital.");
        }
    }
    private void assertOwned(UUID hospitalId, DataAccessScope scope) {
        if (scope.administrator()) return;
        HospitalContext selected = hospital(scope, null);
        if (!hospitalId.equals(selected.id())) throw new AccountingAccessDeniedException();
    }

    private AccountingAccountResponse toAccount(AccountingAccountEntity item) {
        return new AccountingAccountResponse(item.getId(), item.getHospitalId(), item.getHospitalCode(), item.getAccountNumber(),
                item.getLabel(), item.getAccountClass(), item.getNature(), item.isActive(), item.isSystemAccount(), item.getCreatedAt());
    }
    private AccountingJournalResponse toJournal(AccountingJournalEntity item) {
        return new AccountingJournalResponse(item.getId(), item.getHospitalId(), item.getHospitalCode(), item.getCode(), item.getLabel(),
                item.getType(), item.isActive(), item.isSystemJournal(), item.getCreatedAt());
    }
    private AccountingPeriodResponse toPeriod(AccountingPeriodEntity item) {
        return new AccountingPeriodResponse(item.getId(), item.getHospitalId(), item.getHospitalCode(), item.getCode(), item.getLabel(),
                item.getStartsOn(), item.getEndsOn(), item.getStatus(), item.getClosedAt(), item.getClosedByUsername(), item.getCreatedAt());
    }
    private AccountingEntryResponse toEntry(AccountingEntryEntity item, List<AccountingEntryLineEntity> lines) {
        return new AccountingEntryResponse(item.getId(), item.getHospitalId(), item.getHospitalCode(), item.getPeriodId(), item.getJournalId(),
                item.getJournalCode(), item.getCode(), item.getSourceType(), item.getSourceCode(), item.getEntryDate(), item.getDescription(),
                item.getStatus(), item.getCurrency(), item.getTotalDebit(), item.getTotalCredit(), item.getCreatedAt(), item.getCreatedByUsername(),
                item.getPostedAt(), item.getPostedByUsername(), item.getReversalEntryId(), lines.stream().map(this::toEntryLine).toList());
    }
    private AccountingEntryLineResponse toEntryLine(AccountingEntryLineEntity item) {
        return new AccountingEntryLineResponse(item.getId(), item.getLineNumber(), item.getAccountId(), item.getAccountNumber(),
                item.getAccountLabel(), item.getLabel(), item.getDebit(), item.getCredit(), item.getThirdPartyReference());
    }
    private AccountingInvoiceResponse toInvoice(AccountingInvoiceEntity item) {
        return new AccountingInvoiceResponse(item.getId(), item.getHospitalId(), item.getHospitalCode(), item.getCode(), item.getSourceType(),
                item.getSourceCode(), item.getPatientId(), item.getPatientCode(), item.getPassageId(), item.getPassageCode(), item.getIssuedOn(),
                item.getStatus(), item.getCurrency(), item.getTotalAmount(), item.getPaidAmount(), item.getDueAmount(), item.getDescription(),
                item.getCreatedAt(), item.getCreatedByUsername());
    }
    private AccountingPaymentResponse toPayment(AccountingPaymentEntity item) {
        return new AccountingPaymentResponse(item.getId(), item.getHospitalId(), item.getHospitalCode(), item.getCode(), item.getInvoiceId(),
                item.getInvoiceCode(), item.getPaidOn(), item.getAmount(), item.getCurrency(), item.getMethod(), item.getPaymentReference(),
                item.getAccountingEntryId(), item.getAccountingEntryCode(), item.getCreatedAt(), item.getReceivedByUsername());
    }
    private FinancialStatementNoteResponse toNote(FinancialStatementNoteEntity item) {
        return new FinancialStatementNoteResponse(item.getId(), item.getHospitalId(), item.getHospitalCode(), item.getPeriodId(), item.getCode(),
                item.getTitle(), item.getType(), item.getContent(), item.getStatus(), item.getCreatedAt(), item.getCreatedByUsername(),
                item.getValidatedAt(), item.getValidatedByUsername());
    }
    private AccountingSupportingDocumentResponse toDocument(AccountingSupportingDocumentEntity item) {
        return new AccountingSupportingDocumentResponse(item.getId(), item.getRelatedType(), item.getRelatedId(), item.getType(), item.getFileName(),
                item.getContentType(), item.getSizeBytes(), item.getUploadedAt(), item.getUploadedByUsername());
    }
    private LedgerRowResponse toLedgerRow(AccountingEntryLineEntity item) {
        AccountingEntryEntity entry = item.getEntry();
        return new LedgerRowResponse(entry.getId(), entry.getCode(), entry.getEntryDate(), entry.getJournalCode(), entry.getDescription(),
                item.getAccountId(), item.getAccountNumber(), item.getAccountLabel(), item.getLabel(), item.getDebit(), item.getCredit(),
                item.getThirdPartyReference());
    }
    private CashSessionResponse toCashSession(CashSessionEntity item) {
        BigDecimal expected = item.getExpectedClosingAmount();
        if (item.getStatus() == CashSessionStatus.OPEN) {
            BigDecimal receipts = paymentRepository.sumReceived(item.getHospitalId(), item.getCurrency(), AccountingPaymentMethod.CASH,
                    item.getOpenedAt(), Instant.now());
            expected = money(item.getOpeningAmount().add(receipts == null ? BigDecimal.ZERO : receipts));
        }
        return new CashSessionResponse(item.getId(), item.getHospitalId(), item.getHospitalCode(), item.getCode(), item.getCurrency(),
                item.getOpeningAmount(), item.getStatus(), item.getOpenedAt(), item.getOpenedByUsername(), item.getClosedAt(), item.getClosedByUsername(),
                expected, item.getDeclaredClosingAmount(), item.getVarianceAmount(), item.getClosingNotes());
    }

    private <T> PageResponse<T> page(org.springframework.data.domain.Page<T> source) {
        return new PageResponse<>(source.getContent(), source.getNumber(), source.getSize(), source.getTotalElements(), source.getTotalPages());
    }
    private PageRequest pageable(int page, int size, String sort) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(sort).descending());
    }
    private String nextCode(String prefix, Predicate<String> exists) {
        for (int attempt = 0; attempt < 8; attempt++) {
            String code = prefix + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (!exists.test(code)) return code;
        }
        throw new IllegalStateException("Impossible de générer un code comptable unique.");
    }
    private String upper(String value) { return value == null ? "" : value.trim().toUpperCase(Locale.ROOT); }
    private AccountingCurrency parseCurrency(String value) {
        if (value == null || value.isBlank()) throw new AccountingValidationException("La devise est absente de la délivrance.");
        try { return AccountingCurrency.valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { throw new AccountingValidationException("La devise de la délivrance est inconnue."); }
    }
    private AccountingPaymentMethod parsePaymentMethod(String value) {
        if (value == null || value.isBlank()) return AccountingPaymentMethod.OTHER;
        try { return AccountingPaymentMethod.valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { return AccountingPaymentMethod.OTHER; }
    }
    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
    private String normalize(String value) { return value == null ? "" : value.trim(); }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String orSystem(String value) { return value == null || value.isBlank() ? "system" : value.trim(); }

    private record HospitalContext(UUID id, String code) { }
    private record LineSpec(AccountingAccountEntity account, String label, BigDecimal debit, BigDecimal credit, String thirdPartyReference) { }
    private record ResolvedLine(AccountingAccountEntity account, String label, BigDecimal debit, BigDecimal credit, String thirdPartyReference) { }
    private record StockMovementPosting(AccountingAccountEntity debitAccount, String debitLabel, String description) { }
    private record Totals(AccountingAccountEntity account, BigDecimal debit, BigDecimal credit) {
        Totals add(BigDecimal nextDebit, BigDecimal nextCredit) { return new Totals(account, debit.add(nextDebit), credit.add(nextCredit)); }
    }
}
