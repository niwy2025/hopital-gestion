package com.hopital.pharmacy.api;

import com.hopital.pharmacy.application.domain.AccountingPostingStatus;
import com.hopital.pharmacy.application.domain.AuditActor;
import com.hopital.pharmacy.application.domain.DataAccessScope;
import com.hopital.pharmacy.application.domain.StockMovementType;
import com.hopital.pharmacy.application.dto.CreateMedicineRequest;
import com.hopital.pharmacy.application.dto.CreateStockIssueRequest;
import com.hopital.pharmacy.application.dto.CreateStockEntryRequest;
import com.hopital.pharmacy.application.dto.ExpiryTreatmentResponse;
import com.hopital.pharmacy.application.dto.MedicineResponse;
import com.hopital.pharmacy.application.dto.PageResponse;
import com.hopital.pharmacy.application.dto.StockBalanceResponse;
import com.hopital.pharmacy.application.dto.StockEntryResponse;
import com.hopital.pharmacy.application.dto.StockAvailabilityResponse;
import com.hopital.pharmacy.application.dto.StockMovementResponse;
import com.hopital.pharmacy.application.service.PharmacyApplicationService;
import com.hopital.pharmacy.infra.integration.auth.AuthAccessScopeClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pharmacy")
public class PharmacyController {
    private final PharmacyApplicationService pharmacyApplicationService;
    private final AuthAccessScopeClient authAccessScopeClient;
    public PharmacyController(PharmacyApplicationService pharmacyApplicationService, AuthAccessScopeClient authAccessScopeClient) {
        this.pharmacyApplicationService = pharmacyApplicationService; this.authAccessScopeClient = authAccessScopeClient;
    }

    @GetMapping("/medicines/search")
    public ResponseEntity<PageResponse<MedicineResponse>> searchMedicines(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "active", required = false) Boolean active) {
        return ResponseEntity.ok(pharmacyApplicationService.searchMedicines(page, size, query, active));
    }

    @PostMapping("/medicines")
    public ResponseEntity<MedicineResponse> createMedicine(@Valid @RequestBody CreateMedicineRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pharmacyApplicationService.createMedicine(request, actor(jwt)));
    }

    @GetMapping("/stocks/search")
    public ResponseEntity<PageResponse<StockBalanceResponse>> searchStocks(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "lowStock", defaultValue = "false") boolean lowStock,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(pharmacyApplicationService.searchStocks(page, size, query, lowStock, scope(jwt)));
    }

    @GetMapping("/stocks/medicines/{medicineId}")
    public ResponseEntity<StockAvailabilityResponse> currentStock(
            @PathVariable("medicineId") UUID medicineId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(pharmacyApplicationService.currentStock(medicineId, scope(jwt)));
    }

    @GetMapping("/stock-entries/search")
    public ResponseEntity<PageResponse<StockEntryResponse>> searchStockEntries(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "accountingStatus", required = false) AccountingPostingStatus accountingStatus,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(pharmacyApplicationService.searchStockEntries(page, size, query, accountingStatus, scope(jwt)));
    }

    @PostMapping("/stock-entries")
    public ResponseEntity<StockEntryResponse> receiveStock(@Valid @RequestBody CreateStockEntryRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pharmacyApplicationService.receiveStock(request, scope(jwt), actor(jwt)));
    }

    @GetMapping("/stock-movements/search")
    public ResponseEntity<PageResponse<StockMovementResponse>> searchStockMovements(
            @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query, @RequestParam(name = "medicineId", required = false) UUID medicineId,
            @RequestParam(name = "type", required = false) StockMovementType type, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(pharmacyApplicationService.searchStockMovements(page, size, query, medicineId, type, scope(jwt)));
    }

    @PostMapping("/stock-exits")
    public ResponseEntity<StockMovementResponse> issueStock(@Valid @RequestBody CreateStockIssueRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pharmacyApplicationService.issueStock(request, scope(jwt), actor(jwt)));
    }

    @PostMapping("/stocks/expire")
    public ResponseEntity<ExpiryTreatmentResponse> processExpiredStock(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(pharmacyApplicationService.processExpiredStock(scope(jwt), actor(jwt)));
    }

    private DataAccessScope scope(Jwt jwt) { return authAccessScopeClient.resolve(username(jwt)); }
    private AuditActor actor(Jwt jwt) { return new AuditActor(jwt.getSubject(), username(jwt)); }
    private String username(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return username == null || username.isBlank() ? jwt.getSubject() : username;
    }
}
