package com.hopital.patient.api;

import com.hopital.patient.application.dto.PatientPassageLaboratoryReferenceResponse;
import com.hopital.patient.application.dto.PharmacyDispenseAccountingReferenceResponse;
import com.hopital.patient.application.dto.PharmacyDispensePaymentSettlementRequest;
import com.hopital.patient.application.dto.PharmacyDispensePaymentSettlementResponse;
import com.hopital.patient.application.service.PatientApplicationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal API, intentionally excluded from the public gateway routes. */
@RestController
@RequestMapping("/internal/patients")
public class InternalPatientController {

    private final PatientApplicationService patientApplicationService;

    public InternalPatientController(PatientApplicationService patientApplicationService) {
        this.patientApplicationService = patientApplicationService;
    }

    @GetMapping("/passages/{passageId}/laboratory-reference")
    public ResponseEntity<PatientPassageLaboratoryReferenceResponse> resolvePassageForLaboratory(
            @PathVariable("passageId") UUID passageId) {
        return ResponseEntity.ok(patientApplicationService.resolvePassageForLaboratory(passageId));
    }

    @GetMapping("/pharmacy-dispensations/{dispenseCode}/accounting-reference")
    public ResponseEntity<PharmacyDispenseAccountingReferenceResponse> resolvePharmacyDispenseForAccounting(
            @PathVariable("dispenseCode") String dispenseCode) {
        return ResponseEntity.ok(patientApplicationService
                .resolvePharmacyDispenseAccountingReference(dispenseCode));
    }

    /**
     * Receives the authoritative, cumulative invoice state after accounting
     * issues a pharmacy invoice or records a later cash settlement. This is
     * intentionally an internal Docker-network contract, never a provider
     * callback exposed through the public gateway.
     */
    @PostMapping("/pharmacy-dispensations/{dispenseCode}/payment-settlements")
    public ResponseEntity<PharmacyDispensePaymentSettlementResponse> applyPharmacyDispensePaymentSettlement(
            @PathVariable("dispenseCode") String dispenseCode,
            @Valid @RequestBody PharmacyDispensePaymentSettlementRequest request) {
        return ResponseEntity.ok(patientApplicationService
                .applyPharmacyDispensePaymentSettlement(dispenseCode, request));
    }
}
