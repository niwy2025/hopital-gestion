package com.hopital.accounting.api;

import com.hopital.accounting.application.dto.PharmacyDispensationAccountingRequest;
import com.hopital.accounting.application.dto.PharmacyDispensationAccountingResponse;
import com.hopital.accounting.application.dto.PharmacyStockReceiptAccountingRequest;
import com.hopital.accounting.application.dto.PharmacyStockReceiptAccountingResponse;
import com.hopital.accounting.application.dto.PharmacyStockMovementAccountingRequest;
import com.hopital.accounting.application.dto.PharmacyStockMovementAccountingResponse;
import com.hopital.accounting.application.service.AccountingApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Docker-network endpoint for the patient-service accounting outbox only. */
@RestController
@RequestMapping("/internal/accounting")
public class InternalAccountingController {
    private final AccountingApplicationService service;
    public InternalAccountingController(AccountingApplicationService service) { this.service = service; }

    @PostMapping("/pharmacy-dispensations")
    public ResponseEntity<PharmacyDispensationAccountingResponse> recordPharmacyDispensation(
            @Valid @RequestBody PharmacyDispensationAccountingRequest request) {
        return ResponseEntity.ok(service.recordPharmacyDispensation(request.dispenseCode()));
    }

    @PostMapping("/pharmacy-stock-entries")
    public ResponseEntity<PharmacyStockReceiptAccountingResponse> recordPharmacyStockReceipt(
            @Valid @RequestBody PharmacyStockReceiptAccountingRequest request) {
        return ResponseEntity.ok(service.recordPharmacyStockReceipt(request.stockEntryCode()));
    }

    @PostMapping("/pharmacy-stock-movements")
    public ResponseEntity<PharmacyStockMovementAccountingResponse> recordPharmacyStockMovement(
            @Valid @RequestBody PharmacyStockMovementAccountingRequest request) {
        return ResponseEntity.ok(service.recordPharmacyStockMovement(request.stockMovementCode()));
    }
}
