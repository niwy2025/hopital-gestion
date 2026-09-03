package com.hopital.accounting.api;

import com.hopital.accounting.application.domain.AccountingEntryStatus;
import com.hopital.accounting.application.domain.AccountingPeriodStatus;
import com.hopital.accounting.application.domain.AccountingPaymentMethod;
import com.hopital.accounting.application.domain.CashSessionStatus;
import com.hopital.accounting.application.domain.DataAccessScope;
import com.hopital.accounting.application.domain.FinancialStatementNoteStatus;
import com.hopital.accounting.application.domain.InvoiceStatus;
import com.hopital.accounting.application.dto.AccountingAccountResponse;
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
import com.hopital.accounting.application.dto.CreateAccountingEntryRequest;
import com.hopital.accounting.application.dto.CreateAccountingInvoiceRequest;
import com.hopital.accounting.application.dto.CreateAccountingJournalRequest;
import com.hopital.accounting.application.dto.CreateAccountingPaymentRequest;
import com.hopital.accounting.application.dto.CreateAccountingPeriodRequest;
import com.hopital.accounting.application.dto.CreateFinancialStatementNoteRequest;
import com.hopital.accounting.application.dto.FinancialStatementNoteResponse;
import com.hopital.accounting.application.dto.FinancialStatementsResponse;
import com.hopital.accounting.application.dto.LedgerRowResponse;
import com.hopital.accounting.application.dto.OpenCashSessionRequest;
import com.hopital.accounting.application.dto.PageResponse;
import com.hopital.accounting.application.dto.ReverseAccountingEntryRequest;
import com.hopital.accounting.application.dto.TrialBalanceResponse;
import com.hopital.accounting.application.dto.UpdateAccountingAccountRequest;
import com.hopital.accounting.application.dto.UpdateAccountingJournalRequest;
import com.hopital.accounting.application.dto.UpdateFinancialStatementNoteRequest;
import com.hopital.accounting.application.dto.UploadAccountingSupportingDocumentRequest;
import com.hopital.accounting.application.domain.AuditActor;
import com.hopital.accounting.application.service.AccountingApplicationService;
import com.hopital.accounting.infra.integration.auth.AuthAccessScopeClient;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounting")
public class AccountingController {
    private static final String FINANCE_READ = "hasAnyRole('ADMIN','HOSPITAL_ADMIN','HOSPITAL_ACCOUNTANT','FINANCE_MANAGER','FINANCE_AUDITOR')";
    private static final String INVOICE_READ = "hasAnyRole('ADMIN','HOSPITAL_ADMIN','HOSPITAL_ACCOUNTANT','FINANCE_MANAGER','FINANCE_AUDITOR','BILLING_OFFICER','CASHIER')";
    private static final String CASH_READ = "hasAnyRole('ADMIN','HOSPITAL_ADMIN','HOSPITAL_ACCOUNTANT','FINANCE_MANAGER','FINANCE_AUDITOR','CASHIER')";
    // HOSPITAL_ADMIN deliberately remains read-only for the financial book.
    // Roles below mirror the permission matrix held by account-service.
    private static final String CONFIG_WRITE = "hasAnyRole('ADMIN','FINANCE_MANAGER')";
    private static final String ENTRY_CREATE = "hasAnyRole('ADMIN','HOSPITAL_ACCOUNTANT','FINANCE_MANAGER')";
    private static final String ENTRY_POST = "hasAnyRole('ADMIN','FINANCE_MANAGER')";
    private static final String INVOICE_CREATE = "hasAnyRole('ADMIN','FINANCE_MANAGER','BILLING_OFFICER')";
    private static final String INVOICE_ISSUE = "hasAnyRole('ADMIN','FINANCE_MANAGER')";
    private static final String CASH_WRITE = "hasAnyRole('ADMIN','CASHIER')";
    private static final String NOTE_WRITE = "hasAnyRole('ADMIN','HOSPITAL_ACCOUNTANT','FINANCE_MANAGER')";
    private static final String NOTE_VALIDATE = "hasAnyRole('ADMIN','FINANCE_MANAGER')";
    private final AccountingApplicationService service;
    private final AuthAccessScopeClient accessScopeClient;

    public AccountingController(AccountingApplicationService service, AuthAccessScopeClient accessScopeClient) {
        this.service = service; this.accessScopeClient = accessScopeClient;
    }

    @GetMapping("/dashboard") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<AccountingDashboardResponse> dashboard(@RequestParam(name = "hospitalId", required = false) UUID hospitalId,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.ok(service.dashboard(hospitalId, scope(jwt))); }

    @GetMapping("/accounts/search") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<PageResponse<AccountingAccountResponse>> searchAccounts(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "hospitalId", required = false) UUID hospitalId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.searchAccounts(page, size, query, active, hospitalId, scope(jwt)));
    }
    @PostMapping("/accounts") @PreAuthorize(CONFIG_WRITE)
    public ResponseEntity<AccountingAccountResponse> createAccount(@Valid @RequestBody CreateAccountingAccountRequest request,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(request, scope(jwt))); }
    @PutMapping("/accounts/{accountId}") @PreAuthorize(CONFIG_WRITE)
    public ResponseEntity<AccountingAccountResponse> updateAccount(@PathVariable("accountId") UUID accountId,
            @Valid @RequestBody UpdateAccountingAccountRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.updateAccount(accountId, request, scope(jwt)));
    }

    @GetMapping("/journals/search") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<PageResponse<AccountingJournalResponse>> searchJournals(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "hospitalId", required = false) UUID hospitalId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.searchJournals(page, size, query, active, hospitalId, scope(jwt)));
    }
    @PostMapping("/journals") @PreAuthorize(CONFIG_WRITE)
    public ResponseEntity<AccountingJournalResponse> createJournal(@Valid @RequestBody CreateAccountingJournalRequest request,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.status(HttpStatus.CREATED).body(service.createJournal(request, scope(jwt))); }
    @PutMapping("/journals/{journalId}") @PreAuthorize(CONFIG_WRITE)
    public ResponseEntity<AccountingJournalResponse> updateJournal(@PathVariable("journalId") UUID journalId,
            @Valid @RequestBody UpdateAccountingJournalRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.updateJournal(journalId, request, scope(jwt)));
    }

    @GetMapping("/periods/search") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<PageResponse<AccountingPeriodResponse>> searchPeriods(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "status", required = false) AccountingPeriodStatus status,
            @RequestParam(name = "hospitalId", required = false) UUID hospitalId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.searchPeriods(page, size, query, status, hospitalId, scope(jwt)));
    }
    @PostMapping("/periods") @PreAuthorize(CONFIG_WRITE)
    public ResponseEntity<AccountingPeriodResponse> createPeriod(@Valid @RequestBody CreateAccountingPeriodRequest request,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.status(HttpStatus.CREATED).body(service.createPeriod(request, scope(jwt))); }
    @PostMapping("/periods/{periodId}/close") @PreAuthorize(CONFIG_WRITE)
    public ResponseEntity<AccountingPeriodResponse> closePeriod(@PathVariable("periodId") UUID periodId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.closePeriod(periodId, scope(jwt), actor(jwt)));
    }

    @GetMapping("/entries/search") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<PageResponse<AccountingEntryResponse>> searchEntries(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "status", required = false) AccountingEntryStatus status,
            @RequestParam(name = "periodId", required = false) UUID periodId, @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) LocalDate dateTo, @RequestParam(name = "hospitalId", required = false) UUID hospitalId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.searchEntries(page, size, query, status, periodId, dateFrom, dateTo, hospitalId, scope(jwt)));
    }
    @GetMapping("/entries/{entryId}") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<AccountingEntryResponse> getEntry(@PathVariable("entryId") UUID entryId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.getEntry(entryId, scope(jwt)));
    }
    @PostMapping("/entries") @PreAuthorize(ENTRY_CREATE)
    public ResponseEntity<AccountingEntryResponse> createEntry(@Valid @RequestBody CreateAccountingEntryRequest request,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.status(HttpStatus.CREATED).body(service.createManualEntry(request, scope(jwt), actor(jwt))); }
    @PostMapping("/entries/{entryId}/post") @PreAuthorize(ENTRY_POST)
    public ResponseEntity<AccountingEntryResponse> postEntry(@PathVariable("entryId") UUID entryId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.postEntry(entryId, scope(jwt), actor(jwt)));
    }
    @PostMapping("/entries/{entryId}/reverse") @PreAuthorize(ENTRY_POST)
    public ResponseEntity<AccountingEntryResponse> reverseEntry(@PathVariable("entryId") UUID entryId,
            @Valid @RequestBody ReverseAccountingEntryRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.reverseEntry(entryId, request, scope(jwt), actor(jwt)));
    }

    @GetMapping("/invoices/search") @PreAuthorize(INVOICE_READ)
    public ResponseEntity<PageResponse<AccountingInvoiceResponse>> searchInvoices(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "status", required = false) InvoiceStatus status,
            @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom, @RequestParam(name = "dateTo", required = false) LocalDate dateTo,
            @RequestParam(name = "hospitalId", required = false) UUID hospitalId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.searchInvoices(page, size, query, status, dateFrom, dateTo, hospitalId, scope(jwt)));
    }
    @GetMapping("/invoices/{invoiceId}") @PreAuthorize(INVOICE_READ)
    public ResponseEntity<AccountingInvoiceResponse> getInvoice(@PathVariable("invoiceId") UUID invoiceId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.getInvoice(invoiceId, scope(jwt)));
    }
    @PostMapping("/invoices") @PreAuthorize(INVOICE_CREATE)
    public ResponseEntity<AccountingInvoiceResponse> createInvoice(@Valid @RequestBody CreateAccountingInvoiceRequest request,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.status(HttpStatus.CREATED).body(service.createManualInvoice(request, scope(jwt), actor(jwt))); }
    @PostMapping("/invoices/{invoiceId}/issue") @PreAuthorize(INVOICE_ISSUE)
    public ResponseEntity<AccountingInvoiceResponse> issueInvoice(@PathVariable("invoiceId") UUID invoiceId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.issueInvoice(invoiceId, scope(jwt), actor(jwt)));
    }
    @PostMapping("/invoices/{invoiceId}/cancel") @PreAuthorize(INVOICE_ISSUE)
    public ResponseEntity<AccountingInvoiceResponse> cancelInvoice(@PathVariable("invoiceId") UUID invoiceId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.cancelInvoice(invoiceId, scope(jwt), actor(jwt)));
    }
    @PostMapping("/invoices/{invoiceId}/payments") @PreAuthorize(CASH_WRITE)
    public ResponseEntity<AccountingPaymentResponse> recordPayment(@PathVariable("invoiceId") UUID invoiceId,
            @Valid @RequestBody CreateAccountingPaymentRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.recordPayment(invoiceId, request, scope(jwt), actor(jwt)));
    }

    @GetMapping("/payments/search") @PreAuthorize(CASH_READ)
    public ResponseEntity<PageResponse<AccountingPaymentResponse>> searchPayments(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "method", required = false) AccountingPaymentMethod method,
            @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) LocalDate dateTo, @RequestParam(name = "hospitalId", required = false) UUID hospitalId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.searchPayments(page, size, query, method, dateFrom, dateTo, hospitalId, scope(jwt)));
    }

    @GetMapping("/ledger/search") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<PageResponse<LedgerRowResponse>> searchLedger(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "accountId", required = false) UUID accountId,
            @RequestParam(name = "periodId", required = false) UUID periodId, @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) LocalDate dateTo, @RequestParam(name = "hospitalId", required = false) UUID hospitalId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.searchLedger(page, size, query, accountId, periodId, dateFrom, dateTo, hospitalId, scope(jwt)));
    }
    @GetMapping("/trial-balance") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<TrialBalanceResponse> trialBalance(@RequestParam(name = "periodId", required = false) UUID periodId,
            @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom, @RequestParam(name = "dateTo", required = false) LocalDate dateTo,
            @RequestParam(name = "hospitalId", required = false) UUID hospitalId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.trialBalance(periodId, dateFrom, dateTo, hospitalId, scope(jwt)));
    }
    @GetMapping("/financial-statements") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<FinancialStatementsResponse> financialStatements(@RequestParam(name = "periodId", required = false) UUID periodId,
            @RequestParam(name = "hospitalId", required = false) UUID hospitalId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.financialStatements(periodId, hospitalId, scope(jwt)));
    }

    @GetMapping("/notes/search") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<PageResponse<FinancialStatementNoteResponse>> searchNotes(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "status", required = false) FinancialStatementNoteStatus status,
            @RequestParam(name = "periodId", required = false) UUID periodId, @RequestParam(name = "hospitalId", required = false) UUID hospitalId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.searchNotes(page, size, query, status, periodId, hospitalId, scope(jwt)));
    }
    @PostMapping("/notes") @PreAuthorize(NOTE_WRITE)
    public ResponseEntity<FinancialStatementNoteResponse> createNote(@Valid @RequestBody CreateFinancialStatementNoteRequest request,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.status(HttpStatus.CREATED).body(service.createNote(request, scope(jwt), actor(jwt))); }
    @PutMapping("/notes/{noteId}") @PreAuthorize(NOTE_WRITE)
    public ResponseEntity<FinancialStatementNoteResponse> updateNote(@PathVariable("noteId") UUID noteId,
            @Valid @RequestBody UpdateFinancialStatementNoteRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.updateNote(noteId, request, scope(jwt)));
    }
    @PostMapping("/notes/{noteId}/validate") @PreAuthorize(NOTE_VALIDATE)
    public ResponseEntity<FinancialStatementNoteResponse> validateNote(@PathVariable("noteId") UUID noteId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.validateNote(noteId, scope(jwt), actor(jwt)));
    }

    @GetMapping("/cash-sessions/search") @PreAuthorize(CASH_READ)
    public ResponseEntity<PageResponse<CashSessionResponse>> searchCashSessions(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "status", required = false) CashSessionStatus status,
            @RequestParam(name = "hospitalId", required = false) UUID hospitalId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.searchCashSessions(page, size, query, status, hospitalId, scope(jwt)));
    }
    @PostMapping("/cash-sessions/open") @PreAuthorize(CASH_WRITE)
    public ResponseEntity<CashSessionResponse> openCashSession(@Valid @RequestBody OpenCashSessionRequest request,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.status(HttpStatus.CREATED).body(service.openCashSession(request, scope(jwt), actor(jwt))); }
    @PostMapping("/cash-sessions/{cashSessionId}/close") @PreAuthorize(CASH_WRITE)
    public ResponseEntity<CashSessionResponse> closeCashSession(@PathVariable("cashSessionId") UUID cashSessionId,
            @Valid @RequestBody CloseCashSessionRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.closeCashSession(cashSessionId, request, scope(jwt), actor(jwt)));
    }

    @GetMapping("/invoices/{invoiceId}/documents") @PreAuthorize(INVOICE_READ)
    public ResponseEntity<List<AccountingSupportingDocumentResponse>> invoiceDocuments(@PathVariable("invoiceId") UUID invoiceId,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.ok(service.listDocuments("INVOICE", invoiceId, scope(jwt))); }
    @PostMapping("/invoices/{invoiceId}/documents") @PreAuthorize(INVOICE_CREATE)
    public ResponseEntity<AccountingSupportingDocumentResponse> uploadInvoiceDocument(@PathVariable("invoiceId") UUID invoiceId,
            @Valid @RequestBody UploadAccountingSupportingDocumentRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.uploadDocument("INVOICE", invoiceId, request, scope(jwt), actor(jwt)));
    }
    @GetMapping("/entries/{entryId}/documents") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<List<AccountingSupportingDocumentResponse>> entryDocuments(@PathVariable("entryId") UUID entryId,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.ok(service.listDocuments("ENTRY", entryId, scope(jwt))); }
    @PostMapping("/entries/{entryId}/documents") @PreAuthorize(ENTRY_CREATE)
    public ResponseEntity<AccountingSupportingDocumentResponse> uploadEntryDocument(@PathVariable("entryId") UUID entryId,
            @Valid @RequestBody UploadAccountingSupportingDocumentRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.uploadDocument("ENTRY", entryId, request, scope(jwt), actor(jwt)));
    }
    @GetMapping("/payments/{paymentId}/documents") @PreAuthorize(CASH_READ)
    public ResponseEntity<List<AccountingSupportingDocumentResponse>> paymentDocuments(@PathVariable("paymentId") UUID paymentId,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.ok(service.listDocuments("PAYMENT", paymentId, scope(jwt))); }
    @PostMapping("/payments/{paymentId}/documents") @PreAuthorize(CASH_WRITE)
    public ResponseEntity<AccountingSupportingDocumentResponse> uploadPaymentDocument(@PathVariable("paymentId") UUID paymentId,
            @Valid @RequestBody UploadAccountingSupportingDocumentRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.uploadDocument("PAYMENT", paymentId, request, scope(jwt), actor(jwt)));
    }
    @GetMapping("/notes/{noteId}/documents") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<List<AccountingSupportingDocumentResponse>> noteDocuments(@PathVariable("noteId") UUID noteId,
            @AuthenticationPrincipal Jwt jwt) { return ResponseEntity.ok(service.listDocuments("NOTE", noteId, scope(jwt))); }
    @PostMapping("/notes/{noteId}/documents") @PreAuthorize(NOTE_WRITE)
    public ResponseEntity<AccountingSupportingDocumentResponse> uploadNoteDocument(@PathVariable("noteId") UUID noteId,
            @Valid @RequestBody UploadAccountingSupportingDocumentRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.uploadDocument("NOTE", noteId, request, scope(jwt), actor(jwt)));
    }
    @GetMapping("/invoices/{invoiceId}/documents/{documentId}/content") @PreAuthorize(INVOICE_READ)
    public ResponseEntity<byte[]> downloadInvoiceDocument(@PathVariable("invoiceId") UUID invoiceId,
            @PathVariable("documentId") UUID documentId, @AuthenticationPrincipal Jwt jwt) {
        return downloadRelatedDocument("INVOICE", invoiceId, documentId, jwt);
    }
    @GetMapping("/payments/{paymentId}/documents/{documentId}/content") @PreAuthorize(CASH_READ)
    public ResponseEntity<byte[]> downloadPaymentDocument(@PathVariable("paymentId") UUID paymentId,
            @PathVariable("documentId") UUID documentId, @AuthenticationPrincipal Jwt jwt) {
        return downloadRelatedDocument("PAYMENT", paymentId, documentId, jwt);
    }
    @GetMapping("/entries/{entryId}/documents/{documentId}/content") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<byte[]> downloadEntryDocument(@PathVariable("entryId") UUID entryId,
            @PathVariable("documentId") UUID documentId, @AuthenticationPrincipal Jwt jwt) {
        return downloadRelatedDocument("ENTRY", entryId, documentId, jwt);
    }
    @GetMapping("/notes/{noteId}/documents/{documentId}/content") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<byte[]> downloadNoteDocument(@PathVariable("noteId") UUID noteId,
            @PathVariable("documentId") UUID documentId, @AuthenticationPrincipal Jwt jwt) {
        return downloadRelatedDocument("NOTE", noteId, documentId, jwt);
    }
    @GetMapping("/documents/{documentId}/content") @PreAuthorize(FINANCE_READ)
    public ResponseEntity<byte[]> downloadDocument(@PathVariable("documentId") UUID documentId, @AuthenticationPrincipal Jwt jwt) {
        var document = service.getDocument(documentId, scope(jwt));
        return documentResponse(document);
    }

    private ResponseEntity<byte[]> downloadRelatedDocument(String relatedType, UUID relatedId, UUID documentId, Jwt jwt) {
        var document = service.getDocument(documentId, scope(jwt));
        if (!relatedType.equals(document.getRelatedType()) || !relatedId.equals(document.getRelatedId())) {
            return ResponseEntity.notFound().build();
        }
        return documentResponse(document);
    }
    private ResponseEntity<byte[]> documentResponse(com.hopital.accounting.infra.persistence.entity.AccountingSupportingDocumentEntity document) {
        MediaType mediaType;
        try { mediaType = MediaType.parseMediaType(document.getContentType()); }
        catch (IllegalArgumentException exception) { mediaType = MediaType.APPLICATION_OCTET_STREAM; }
        return ResponseEntity.ok().contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(document.getFileName()).build().toString())
                .body(java.util.Base64.getMimeDecoder().decode(document.getContentBase64()));
    }

    private DataAccessScope scope(Jwt jwt) { return accessScopeClient.resolve(username(jwt)); }
    private AuditActor actor(Jwt jwt) { return new AuditActor(jwt.getSubject(), username(jwt)); }
    private String username(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return username == null || username.isBlank() ? jwt.getSubject() : username;
    }
}
