package com.hopital.pharmacy.api;

import com.hopital.pharmacy.application.domain.AuditActor;
import com.hopital.pharmacy.application.dto.PrescriptionStockDispenseRequest;
import com.hopital.pharmacy.application.service.PharmacyApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<Void> recordPrescriptionDispensation(
            @Valid @RequestBody PrescriptionStockDispenseRequest request) {
        pharmacyApplicationService.recordPrescriptionDispensation(
                request.hospitalId(),
                request.dispenseCode(),
                new AuditActor(request.actorId(), request.actorUsername()),
                request.items());
        return ResponseEntity.noContent().build();
    }
}
