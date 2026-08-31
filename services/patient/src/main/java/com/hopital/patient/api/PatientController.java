package com.hopital.patient.api;

import com.hopital.patient.application.dto.CreatePatientRequest;
import com.hopital.patient.application.dto.PatientDuplicateCheckRequest;
import com.hopital.patient.application.dto.PatientDuplicateCheckResponse;
import com.hopital.patient.application.dto.PatientResponse;
import com.hopital.patient.application.dto.PatientSummaryResponse;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.UpdatePatientStatusRequest;
import com.hopital.patient.application.service.PatientApplicationService;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.infra.integration.auth.AuthAccessScopeClient;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientApplicationService patientApplicationService;
    private final AuthAccessScopeClient authAccessScopeClient;

    public PatientController(PatientApplicationService patientApplicationService, AuthAccessScopeClient authAccessScopeClient) {
        this.patientApplicationService = patientApplicationService;
        this.authAccessScopeClient = authAccessScopeClient;
    }

    @GetMapping
    public ResponseEntity<List<PatientSummaryResponse>> listPatients(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(patientApplicationService.listPatients(accessScope(jwt)));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<PatientSummaryResponse>> searchPatients(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "hospitalId", required = false) UUID hospitalId,
            @RequestParam(name = "active", required = false) Boolean active,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(patientApplicationService.searchPatients(page, size, query, hospitalId, active, accessScope(jwt)));
    }

    @PostMapping("/duplicate-check")
    public ResponseEntity<PatientDuplicateCheckResponse> checkDuplicates(
            @Valid @RequestBody PatientDuplicateCheckRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(patientApplicationService.checkDuplicates(request, accessScope(jwt)));
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody CreatePatientRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientApplicationService.createPatient(request, accessScope(jwt)));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> getPatient(
            @PathVariable("patientId") UUID patientId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(patientApplicationService.getPatient(patientId, accessScope(jwt)));
    }

    @PatchMapping("/{patientId}/status")
    public ResponseEntity<PatientResponse> updateStatus(
            @PathVariable("patientId") UUID patientId,
            @Valid @RequestBody UpdatePatientStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(patientApplicationService.updateStatus(patientId, request, accessScope(jwt)));
    }

    private DataAccessScope accessScope(Jwt jwt) {
        return authAccessScopeClient.resolve(jwt.getClaimAsString("preferred_username"));
    }
}
