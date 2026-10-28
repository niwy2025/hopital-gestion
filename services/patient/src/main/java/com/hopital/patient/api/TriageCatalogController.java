package com.hopital.patient.api;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.dto.CreateHospitalTriageAlertSignRequest;
import com.hopital.patient.application.dto.CreateHospitalTriageMeasurementRequest;
import com.hopital.patient.application.dto.HospitalTriageAlertSignResponse;
import com.hopital.patient.application.dto.HospitalTriageMeasurementResponse;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.UpdateHospitalTriageAlertSignRequest;
import com.hopital.patient.application.dto.UpdateHospitalTriageMeasurementRequest;
import com.hopital.patient.application.dto.UpdateTriageCatalogStatusRequest;
import com.hopital.patient.application.service.TriageCatalogApplicationService;
import com.hopital.patient.infra.integration.auth.AuthAccessScopeClient;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients/triage-catalog")
public class TriageCatalogController {

    private final TriageCatalogApplicationService triageCatalogApplicationService;
    private final AuthAccessScopeClient authAccessScopeClient;

    public TriageCatalogController(
            TriageCatalogApplicationService triageCatalogApplicationService,
            AuthAccessScopeClient authAccessScopeClient) {
        this.triageCatalogApplicationService = triageCatalogApplicationService;
        this.authAccessScopeClient = authAccessScopeClient;
    }

    @GetMapping("/{hospitalId}/measurements/search")
    public ResponseEntity<PageResponse<HospitalTriageMeasurementResponse>> searchMeasurements(
            @PathVariable("hospitalId") UUID hospitalId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "active", required = false) Boolean active,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(triageCatalogApplicationService.searchMeasurements(
                hospitalId, page, size, query, active, accessScope(jwt)));
    }

    @GetMapping("/{hospitalId}/measurements/active")
    public ResponseEntity<List<HospitalTriageMeasurementResponse>> listActiveMeasurements(
            @PathVariable("hospitalId") UUID hospitalId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(triageCatalogApplicationService.listActiveMeasurements(hospitalId, accessScope(jwt)));
    }

    @PostMapping("/{hospitalId}/measurements")
    public ResponseEntity<HospitalTriageMeasurementResponse> createMeasurement(
            @PathVariable("hospitalId") UUID hospitalId,
            @Valid @RequestBody CreateHospitalTriageMeasurementRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(triageCatalogApplicationService.createMeasurement(
                hospitalId, request, accessScope(jwt), auditActor(jwt)));
    }

    @PutMapping("/{hospitalId}/measurements/{measurementId}")
    public ResponseEntity<HospitalTriageMeasurementResponse> updateMeasurement(
            @PathVariable("hospitalId") UUID hospitalId,
            @PathVariable("measurementId") UUID measurementId,
            @Valid @RequestBody UpdateHospitalTriageMeasurementRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(triageCatalogApplicationService.updateMeasurement(
                hospitalId, measurementId, request, accessScope(jwt), auditActor(jwt)));
    }

    @PatchMapping("/{hospitalId}/measurements/{measurementId}/status")
    public ResponseEntity<HospitalTriageMeasurementResponse> updateMeasurementStatus(
            @PathVariable("hospitalId") UUID hospitalId,
            @PathVariable("measurementId") UUID measurementId,
            @Valid @RequestBody UpdateTriageCatalogStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(triageCatalogApplicationService.updateMeasurementStatus(
                hospitalId, measurementId, request, accessScope(jwt), auditActor(jwt)));
    }

    @GetMapping("/{hospitalId}/alert-signs/search")
    public ResponseEntity<PageResponse<HospitalTriageAlertSignResponse>> searchAlertSigns(
            @PathVariable("hospitalId") UUID hospitalId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "active", required = false) Boolean active,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(triageCatalogApplicationService.searchAlertSigns(
                hospitalId, page, size, query, active, accessScope(jwt)));
    }

    @GetMapping("/{hospitalId}/alert-signs/active")
    public ResponseEntity<List<HospitalTriageAlertSignResponse>> listActiveAlertSigns(
            @PathVariable("hospitalId") UUID hospitalId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(triageCatalogApplicationService.listActiveAlertSigns(hospitalId, accessScope(jwt)));
    }

    @PostMapping("/{hospitalId}/alert-signs")
    public ResponseEntity<HospitalTriageAlertSignResponse> createAlertSign(
            @PathVariable("hospitalId") UUID hospitalId,
            @Valid @RequestBody CreateHospitalTriageAlertSignRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(triageCatalogApplicationService.createAlertSign(
                hospitalId, request, accessScope(jwt), auditActor(jwt)));
    }

    @PutMapping("/{hospitalId}/alert-signs/{alertSignId}")
    public ResponseEntity<HospitalTriageAlertSignResponse> updateAlertSign(
            @PathVariable("hospitalId") UUID hospitalId,
            @PathVariable("alertSignId") UUID alertSignId,
            @Valid @RequestBody UpdateHospitalTriageAlertSignRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(triageCatalogApplicationService.updateAlertSign(
                hospitalId, alertSignId, request, accessScope(jwt), auditActor(jwt)));
    }

    @PatchMapping("/{hospitalId}/alert-signs/{alertSignId}/status")
    public ResponseEntity<HospitalTriageAlertSignResponse> updateAlertSignStatus(
            @PathVariable("hospitalId") UUID hospitalId,
            @PathVariable("alertSignId") UUID alertSignId,
            @Valid @RequestBody UpdateTriageCatalogStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(triageCatalogApplicationService.updateAlertSignStatus(
                hospitalId, alertSignId, request, accessScope(jwt), auditActor(jwt)));
    }

    private DataAccessScope accessScope(Jwt jwt) {
        return authAccessScopeClient.resolve(jwt.getClaimAsString("preferred_username"));
    }

    private AuditActor auditActor(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return new AuditActor(
                jwt.getSubject(),
                username == null || username.isBlank() ? "Utilisateur authentifié" : username);
    }
}
