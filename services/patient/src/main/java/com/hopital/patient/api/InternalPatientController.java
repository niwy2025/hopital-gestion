package com.hopital.patient.api;

import com.hopital.patient.application.dto.PatientPassageLaboratoryReferenceResponse;
import com.hopital.patient.application.service.PatientApplicationService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
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
}
