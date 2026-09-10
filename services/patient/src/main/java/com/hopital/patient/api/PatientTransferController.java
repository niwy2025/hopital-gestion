package com.hopital.patient.api;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.PatientTransferStatus;
import com.hopital.patient.application.dto.CancelPatientTransferRequest;
import com.hopital.patient.application.dto.CreatePatientTransferRequest;
import com.hopital.patient.application.dto.ReceivePatientTransferRequest;
import com.hopital.patient.application.dto.PatientTransferResponse;
import com.hopital.patient.application.dto.PatientTransferSummaryResponse;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.service.PatientTransferService;
import com.hopital.patient.infra.integration.auth.AuthAccessScopeClient;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientTransferController {
    private final PatientTransferService service;
    private final AuthAccessScopeClient scopes;

    public PatientTransferController(PatientTransferService service, AuthAccessScopeClient scopes) {
        this.service = service;
        this.scopes = scopes;
    }

    @GetMapping("/transfers/search")
    public PageResponse<PatientTransferSummaryResponse> search(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "status", required = false) PatientTransferStatus status,
            @RequestParam(name = "direction", required = false) String direction,
            @RequestParam(name = "hospitalId", required = false) UUID hospitalId,
            @RequestParam(name = "passageId", required = false) UUID passageId,
            @RequestParam(name = "patientId", required = false) UUID patientId,
            @AuthenticationPrincipal Jwt jwt) {
        return service.search(page, size, query, status, direction, hospitalId, passageId, patientId,
                scopes.resolve(jwt.getClaimAsString("preferred_username")));
    }

    @GetMapping("/transfers/{id}")
    public PatientTransferResponse get(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.get(id, scopes.resolve(jwt.getClaimAsString("preferred_username")));
    }

    @PostMapping("/passages/{passageId}/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientTransferResponse create(@PathVariable("passageId") UUID passageId,
            @Valid @RequestBody CreatePatientTransferRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.create(passageId, request, scopes.resolve(jwt.getClaimAsString("preferred_username")), actor(jwt));
    }

    @PostMapping("/transfers/{id}/dispatch")
    public PatientTransferResponse dispatch(@PathVariable("id") UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.dispatch(id, scopes.resolve(jwt.getClaimAsString("preferred_username")), actor(jwt));
    }

    @PostMapping("/transfers/{id}/receive")
    public PatientTransferResponse receive(@PathVariable("id") UUID id,
            @Valid @RequestBody ReceivePatientTransferRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.receive(id, request, scopes.resolve(jwt.getClaimAsString("preferred_username")), actor(jwt));
    }

    @PostMapping("/transfers/{id}/cancel")
    public PatientTransferResponse cancel(@PathVariable("id") UUID id,
            @Valid @RequestBody CancelPatientTransferRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.cancel(id, request.reason(), scopes.resolve(jwt.getClaimAsString("preferred_username")), actor(jwt));
    }

    private AuditActor actor(Jwt jwt) {
        return new AuditActor(jwt.getSubject(), jwt.getClaimAsString("preferred_username"));
    }
}
