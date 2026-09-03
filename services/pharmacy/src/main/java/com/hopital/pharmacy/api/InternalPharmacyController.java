package com.hopital.pharmacy.api;

import com.hopital.pharmacy.application.domain.AuditActor;
import com.hopital.pharmacy.application.dto.PrescriptionStockDispenseRequest;
import com.hopital.pharmacy.application.dto.PrescriptionDispenseAccountingReferenceResponse;
import com.hopital.pharmacy.application.dto.PrescriptionDispenseValuationResponse;
import com.hopital.pharmacy.application.dto.StockEntryAccountingReferenceResponse;
import com.hopital.pharmacy.application.dto.StockMovementAccountingReferenceResponse;
import com.hopital.pharmacy.application.service.PharmacyApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Docker-network endpoint. It is deliberately not routed through the public gateway. */
@RestController
@RequestMapping("/internal/pharmacy")
public class InternalPharmacyController {
    private final PharmacyApplicationService pharmacyApplicationService;

    public InternalPharmacyController(PharmacyApplicationService pharmacyApplicationService) {
        this.pharmacyApplicationService = pharmacyApplicationService;
    }

    @PostMapping("/prescription-dispensations")
    public ResponseEntity<PrescriptionDispenseValuationResponse> recordPrescriptionDispensation(
            @Valid @RequestBody PrescriptionStockDispenseRequest request) {
        return ResponseEntity.ok(pharmacyApplicationService.recordPrescriptionDispensation(
                request.hospitalId(),
                request.dispenseCode(),
                new AuditActor(request.actorId(), request.actorUsername()),
                request.paidAmount(),
                request.paymentCurrency(),
                request.items()));
    }

    @GetMapping("/prescription-dispensations/{dispenseCode}/accounting-reference")
    public ResponseEntity<PrescriptionDispenseAccountingReferenceResponse> resolveAccountingReference(
            @PathVariable("dispenseCode") String dispenseCode) {
        return ResponseEntity.ok(pharmacyApplicationService
                .resolvePrescriptionDispenseAccountingReference(dispenseCode));
    }

    @GetMapping("/stock-entries/{stockEntryCode}/accounting-reference")
    public ResponseEntity<StockEntryAccountingReferenceResponse> resolveStockEntryAccountingReference(
            @PathVariable("stockEntryCode") String stockEntryCode) {
        return ResponseEntity.ok(pharmacyApplicationService.resolveStockEntryAccountingReference(stockEntryCode));
    }

    @GetMapping("/stock-movements/{stockMovementCode}/accounting-reference")
    public ResponseEntity<StockMovementAccountingReferenceResponse> resolveStockMovementAccountingReference(
            @PathVariable("stockMovementCode") String stockMovementCode) {
        return ResponseEntity.ok(pharmacyApplicationService
                .resolveStockMovementAccountingReference(stockMovementCode));
    }
}
