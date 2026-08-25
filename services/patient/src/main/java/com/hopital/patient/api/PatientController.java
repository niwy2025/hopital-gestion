package com.hopital.patient.api;

import com.hopital.patient.application.dto.CreatePatientRequest;
import com.hopital.patient.application.dto.PatientResponse;
import com.hopital.patient.application.dto.UpdatePatientStatusRequest;
import com.hopital.patient.application.service.PatientApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientApplicationService patientApplicationService;

    public PatientController(PatientApplicationService patientApplicationService) {
        this.patientApplicationService = patientApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> listPatients() {
        return ResponseEntity.ok(patientApplicationService.listPatients());
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientApplicationService.createPatient(request));
    }

    @PatchMapping("/{patientCode}/status")
    public ResponseEntity<PatientResponse> updateStatus(
            @PathVariable("patientCode") String patientCode,
            @Valid @RequestBody UpdatePatientStatusRequest request) {
        return ResponseEntity.ok(patientApplicationService.updateStatus(patientCode, request));
    }
}
