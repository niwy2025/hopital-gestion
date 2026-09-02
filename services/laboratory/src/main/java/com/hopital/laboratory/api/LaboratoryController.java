package com.hopital.laboratory.api;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.DataAccessScope;
import com.hopital.laboratory.application.dto.AnalysisRequestResponse;
import com.hopital.laboratory.application.dto.AnalysisRequestDetailResponse;
import com.hopital.laboratory.application.dto.AnalysisResultResponse;
import com.hopital.laboratory.application.dto.CreateAnalysisRequestRequest;
import com.hopital.laboratory.application.dto.CreateAnalysisResultRequest;
import com.hopital.laboratory.application.dto.CreatePatientPassageAnalysisRequest;
import com.hopital.laboratory.application.dto.CreateReferenceSpecimenCollectionRequest;
import com.hopital.laboratory.application.dto.CreateSpecimenRequest;
import com.hopital.laboratory.application.dto.DispatchReferenceSpecimenRequest;
import com.hopital.laboratory.application.dto.HospitalLaboratoryOptionResponse;
import com.hopital.laboratory.application.dto.PageResponse;
import com.hopital.laboratory.application.dto.PatientPassageLaboratoryRequestResponse;
import com.hopital.laboratory.application.dto.ReferenceLaboratoryOptionResponse;
import com.hopital.laboratory.application.dto.ReceiveReferenceSpecimenRequest;
import com.hopital.laboratory.application.dto.RejectReferenceSpecimenRequest;
import com.hopital.laboratory.application.dto.SpecimenResponse;
import com.hopital.laboratory.application.dto.SpecimenDetailResponse;
import com.hopital.laboratory.application.dto.ValidateAnalysisResultRequest;
import com.hopital.laboratory.application.service.LaboratoryApplicationService;
import com.hopital.laboratory.infra.integration.auth.AuthAccessScopeClient;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/laboratory")
public class LaboratoryController {

    private final LaboratoryApplicationService laboratoryApplicationService;
    private final AuthAccessScopeClient authAccessScopeClient;

    public LaboratoryController(
            LaboratoryApplicationService laboratoryApplicationService,
            AuthAccessScopeClient authAccessScopeClient) {
        this.laboratoryApplicationService = laboratoryApplicationService;
        this.authAccessScopeClient = authAccessScopeClient;
    }

    @GetMapping("/analysis-requests")
    public ResponseEntity<List<AnalysisRequestResponse>> listAnalysisRequests(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.listAnalysisRequests(scope(jwt)));
    }

    @GetMapping("/analysis-requests/search")
    public ResponseEntity<PageResponse<AnalysisRequestResponse>> searchAnalysisRequests(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.searchAnalysisRequests(page, size, query, scope(jwt)));
    }

    /**
     * Dedicated queue for physical samples awaiting reception by their
     * destination reference laboratory. An originating hospital cannot obtain
     * an item through this route with its hospital-only data scope.
     */
    @GetMapping("/analysis-requests/reference-receptions/search")
    public ResponseEntity<PageResponse<AnalysisRequestResponse>> searchReferenceReceptionRequests(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                laboratoryApplicationService.searchReferenceReceptionRequests(page, size, query, scope(jwt)));
    }

    @GetMapping("/analysis-requests/{analysisRequestCode}")
    public ResponseEntity<AnalysisRequestDetailResponse> getAnalysisRequestDetail(
            @PathVariable("analysisRequestCode") String analysisRequestCode,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.getAnalysisRequestDetail(analysisRequestCode, scope(jwt)));
    }

    @GetMapping("/patient-passages/{passageId}/hospital-laboratories")
    public ResponseEntity<List<HospitalLaboratoryOptionResponse>> listHospitalLaboratoriesForPassage(
            @PathVariable("passageId") UUID passageId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.listHospitalLaboratoriesForPassage(passageId, scope(jwt)));
    }

    @GetMapping("/patient-passages/{passageId}/reference-laboratories")
    public ResponseEntity<List<ReferenceLaboratoryOptionResponse>> listReferenceLaboratoriesForPassage(
            @PathVariable("passageId") UUID passageId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.listReferenceLaboratoriesForPassage(passageId, scope(jwt)));
    }

    @GetMapping("/patient-passages/{passageId}/analysis-requests/search")
    public ResponseEntity<PageResponse<PatientPassageLaboratoryRequestResponse>> searchPatientPassageAnalysisRequests(
            @PathVariable("passageId") UUID passageId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "status", required = false) AnalysisRequestStatus status,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.searchPatientPassageAnalysisRequests(
                passageId, page, size, query, status, scope(jwt)));
    }

    @PostMapping("/analysis-requests")
    public ResponseEntity<AnalysisRequestResponse> createAnalysisRequest(
            @Valid @RequestBody CreateAnalysisRequestRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                laboratoryApplicationService.createAnalysisRequest(request, requesterName(jwt), scope(jwt)));
    }

    @PostMapping("/patient-passages/{passageId}/analysis-requests")
    public ResponseEntity<AnalysisRequestResponse> createPatientPassageAnalysisRequest(
                @PathVariable("passageId") UUID passageId,
                @Valid @RequestBody CreatePatientPassageAnalysisRequest request,
                @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.createPatientPassageAnalysisRequest(
                passageId,
                request,
                requesterName(jwt),
                scope(jwt)));
    }

    @GetMapping("/specimens")
    public ResponseEntity<List<SpecimenResponse>> listSpecimens(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.listSpecimens(scope(jwt)));
    }

    @GetMapping("/specimens/search")
    public ResponseEntity<PageResponse<SpecimenResponse>> searchSpecimens(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.searchSpecimens(page, size, query, scope(jwt)));
    }

    @GetMapping("/specimens/{specimenCode}")
    public ResponseEntity<SpecimenDetailResponse> getSpecimenDetail(
            @PathVariable("specimenCode") String specimenCode,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.getSpecimenDetail(specimenCode, scope(jwt)));
    }

    @PostMapping("/specimens")
    public ResponseEntity<SpecimenResponse> receiveSpecimen(
            @Valid @RequestBody CreateSpecimenRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                laboratoryApplicationService.receiveSpecimen(request, requesterName(jwt), scope(jwt)));
    }

    @PostMapping("/patient-passages/{passageId}/analysis-requests/{analysisRequestCode}/specimens")
    public ResponseEntity<SpecimenResponse> receivePatientPassageSpecimen(
            @PathVariable("passageId") UUID passageId,
            @PathVariable("analysisRequestCode") String analysisRequestCode,
            @Valid @RequestBody CreateSpecimenRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.receiveSpecimenForPatientPassage(
                passageId, analysisRequestCode, request, requesterName(jwt), scope(jwt)));
    }

    @PostMapping("/patient-passages/{passageId}/analysis-requests/{analysisRequestCode}/reference-specimens")
    public ResponseEntity<SpecimenResponse> collectReferenceSpecimenForPatientPassage(
            @PathVariable("passageId") UUID passageId,
            @PathVariable("analysisRequestCode") String analysisRequestCode,
            @Valid @RequestBody CreateReferenceSpecimenCollectionRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                laboratoryApplicationService.collectReferenceSpecimenForPatientPassage(
                        passageId, analysisRequestCode, request, requesterName(jwt), scope(jwt)));
    }

    @PostMapping("/patient-passages/{passageId}/analysis-requests/{analysisRequestCode}/reference-specimens/{specimenCode}/dispatch")
    public ResponseEntity<SpecimenResponse> dispatchReferenceSpecimenForPatientPassage(
            @PathVariable("passageId") UUID passageId,
            @PathVariable("analysisRequestCode") String analysisRequestCode,
            @PathVariable("specimenCode") String specimenCode,
            @Valid @RequestBody DispatchReferenceSpecimenRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.dispatchReferenceSpecimenForPatientPassage(
                passageId, analysisRequestCode, specimenCode, request, requesterName(jwt), scope(jwt)));
    }

    @PostMapping("/analysis-requests/{analysisRequestCode}/reference-specimens/{specimenCode}/receive")
    public ResponseEntity<SpecimenResponse> receiveReferenceSpecimen(
            @PathVariable("analysisRequestCode") String analysisRequestCode,
            @PathVariable("specimenCode") String specimenCode,
            @Valid @RequestBody ReceiveReferenceSpecimenRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.receiveReferenceSpecimen(
                analysisRequestCode, specimenCode, request, requesterName(jwt), scope(jwt)));
    }

    @PostMapping("/analysis-requests/{analysisRequestCode}/reference-specimens/{specimenCode}/reject")
    public ResponseEntity<SpecimenResponse> rejectReferenceSpecimen(
            @PathVariable("analysisRequestCode") String analysisRequestCode,
            @PathVariable("specimenCode") String specimenCode,
            @Valid @RequestBody RejectReferenceSpecimenRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.rejectReferenceSpecimen(
                analysisRequestCode, specimenCode, request, requesterName(jwt), scope(jwt)));
    }

    @GetMapping("/analysis-results")
    public ResponseEntity<List<AnalysisResultResponse>> listAnalysisResults(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.listAnalysisResults(scope(jwt)));
    }

    @GetMapping("/analysis-results/search")
    public ResponseEntity<PageResponse<AnalysisResultResponse>> searchAnalysisResults(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.searchAnalysisResults(page, size, query, scope(jwt)));
    }

    @PostMapping("/analysis-results")
    public ResponseEntity<AnalysisResultResponse> enterAnalysisResult(
            @Valid @RequestBody CreateAnalysisResultRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                laboratoryApplicationService.enterAnalysisResult(request, requesterName(jwt), scope(jwt)));
    }

    @PostMapping("/patient-passages/{passageId}/analysis-requests/{analysisRequestCode}/results")
    public ResponseEntity<AnalysisResultResponse> enterPatientPassageAnalysisResult(
            @PathVariable("passageId") UUID passageId,
            @PathVariable("analysisRequestCode") String analysisRequestCode,
            @Valid @RequestBody CreateAnalysisResultRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.enterAnalysisResultForPatientPassage(
                passageId, analysisRequestCode, request, requesterName(jwt), scope(jwt)));
    }

    @PatchMapping("/analysis-results/{resultCode}/validation")
    public ResponseEntity<AnalysisResultResponse> validateAnalysisResult(
            @PathVariable("resultCode") String resultCode,
            @Valid @RequestBody ValidateAnalysisResultRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.validateAnalysisResult(
                resultCode, requesterName(jwt), scope(jwt)));
    }

    @PatchMapping("/patient-passages/{passageId}/analysis-requests/{analysisRequestCode}/results/{resultCode}/validation")
    public ResponseEntity<AnalysisResultResponse> validatePatientPassageAnalysisResult(
            @PathVariable("passageId") UUID passageId,
            @PathVariable("analysisRequestCode") String analysisRequestCode,
            @PathVariable("resultCode") String resultCode,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(laboratoryApplicationService.validateAnalysisResultForPatientPassage(
                passageId,
                analysisRequestCode,
                resultCode,
                requesterName(jwt),
                scope(jwt)));
    }

    private String requesterName(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return username == null || username.isBlank() ? jwt.getSubject() : username;
    }

    private DataAccessScope scope(Jwt jwt) {
        return authAccessScopeClient.resolve(requesterName(jwt));
    }
}
