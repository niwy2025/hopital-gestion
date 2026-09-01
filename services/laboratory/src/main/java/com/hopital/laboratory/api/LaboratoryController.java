package com.hopital.laboratory.api;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.dto.AnalysisRequestResponse;
import com.hopital.laboratory.application.dto.AnalysisRequestDetailResponse;
import com.hopital.laboratory.application.dto.AnalysisResultResponse;
import com.hopital.laboratory.application.dto.CreateAnalysisRequestRequest;
import com.hopital.laboratory.application.dto.CreateAnalysisResultRequest;
import com.hopital.laboratory.application.dto.CreatePatientPassageAnalysisRequest;
import com.hopital.laboratory.application.dto.CreateSpecimenRequest;
import com.hopital.laboratory.application.dto.HospitalLaboratoryOptionResponse;
import com.hopital.laboratory.application.dto.PageResponse;
import com.hopital.laboratory.application.dto.PatientPassageLaboratoryRequestResponse;
import com.hopital.laboratory.application.dto.SpecimenResponse;
import com.hopital.laboratory.application.dto.ValidateAnalysisResultRequest;
import com.hopital.laboratory.application.service.LaboratoryApplicationService;
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

    public LaboratoryController(LaboratoryApplicationService laboratoryApplicationService) {
        this.laboratoryApplicationService = laboratoryApplicationService;
    }

    @GetMapping("/analysis-requests")
    public ResponseEntity<List<AnalysisRequestResponse>> listAnalysisRequests() {
        return ResponseEntity.ok(laboratoryApplicationService.listAnalysisRequests());
    }

    @GetMapping("/analysis-requests/search")
    public ResponseEntity<PageResponse<AnalysisRequestResponse>> searchAnalysisRequests(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query) {
        return ResponseEntity.ok(laboratoryApplicationService.searchAnalysisRequests(page, size, query));
    }

    @GetMapping("/analysis-requests/{analysisRequestCode}")
    public ResponseEntity<AnalysisRequestDetailResponse> getAnalysisRequestDetail(
            @PathVariable("analysisRequestCode") String analysisRequestCode) {
        return ResponseEntity.ok(laboratoryApplicationService.getAnalysisRequestDetail(analysisRequestCode));
    }

    @GetMapping("/patient-passages/{passageId}/hospital-laboratories")
    public ResponseEntity<List<HospitalLaboratoryOptionResponse>> listHospitalLaboratoriesForPassage(
            @PathVariable("passageId") UUID passageId) {
        return ResponseEntity.ok(laboratoryApplicationService.listHospitalLaboratoriesForPassage(passageId));
    }

    @GetMapping("/patient-passages/{passageId}/analysis-requests/search")
    public ResponseEntity<PageResponse<PatientPassageLaboratoryRequestResponse>> searchPatientPassageAnalysisRequests(
            @PathVariable("passageId") UUID passageId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "status", required = false) AnalysisRequestStatus status) {
        return ResponseEntity.ok(laboratoryApplicationService.searchPatientPassageAnalysisRequests(
                passageId, page, size, query, status));
    }

    @PostMapping("/analysis-requests")
    public ResponseEntity<AnalysisRequestResponse> createAnalysisRequest(
            @Valid @RequestBody CreateAnalysisRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.createAnalysisRequest(request));
    }

    @PostMapping("/patient-passages/{passageId}/analysis-requests")
    public ResponseEntity<AnalysisRequestResponse> createPatientPassageAnalysisRequest(
            @PathVariable("passageId") UUID passageId,
            @Valid @RequestBody CreatePatientPassageAnalysisRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.createPatientPassageAnalysisRequest(
                passageId,
                request,
                requesterName(jwt)));
    }

    @GetMapping("/specimens")
    public ResponseEntity<List<SpecimenResponse>> listSpecimens() {
        return ResponseEntity.ok(laboratoryApplicationService.listSpecimens());
    }

    @GetMapping("/specimens/search")
    public ResponseEntity<PageResponse<SpecimenResponse>> searchSpecimens(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query) {
        return ResponseEntity.ok(laboratoryApplicationService.searchSpecimens(page, size, query));
    }

    @PostMapping("/specimens")
    public ResponseEntity<SpecimenResponse> receiveSpecimen(@Valid @RequestBody CreateSpecimenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.receiveSpecimen(request));
    }

    @PostMapping("/patient-passages/{passageId}/analysis-requests/{analysisRequestCode}/specimens")
    public ResponseEntity<SpecimenResponse> receivePatientPassageSpecimen(
            @PathVariable("passageId") UUID passageId,
            @PathVariable("analysisRequestCode") String analysisRequestCode,
            @Valid @RequestBody CreateSpecimenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.receiveSpecimenForPatientPassage(
                passageId, analysisRequestCode, request));
    }

    @GetMapping("/analysis-results")
    public ResponseEntity<List<AnalysisResultResponse>> listAnalysisResults() {
        return ResponseEntity.ok(laboratoryApplicationService.listAnalysisResults());
    }

    @GetMapping("/analysis-results/search")
    public ResponseEntity<PageResponse<AnalysisResultResponse>> searchAnalysisResults(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query) {
        return ResponseEntity.ok(laboratoryApplicationService.searchAnalysisResults(page, size, query));
    }

    @PostMapping("/analysis-results")
    public ResponseEntity<AnalysisResultResponse> enterAnalysisResult(
            @Valid @RequestBody CreateAnalysisResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.enterAnalysisResult(request));
    }

    @PostMapping("/patient-passages/{passageId}/analysis-requests/{analysisRequestCode}/results")
    public ResponseEntity<AnalysisResultResponse> enterPatientPassageAnalysisResult(
            @PathVariable("passageId") UUID passageId,
            @PathVariable("analysisRequestCode") String analysisRequestCode,
            @Valid @RequestBody CreateAnalysisResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.enterAnalysisResultForPatientPassage(
                passageId, analysisRequestCode, request));
    }

    @PatchMapping("/analysis-results/{resultCode}/validation")
    public ResponseEntity<AnalysisResultResponse> validateAnalysisResult(
            @PathVariable("resultCode") String resultCode,
            @Valid @RequestBody ValidateAnalysisResultRequest request) {
        return ResponseEntity.ok(laboratoryApplicationService.validateAnalysisResult(resultCode, request));
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
                requesterName(jwt)));
    }

    private String requesterName(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return username == null || username.isBlank() ? jwt.getSubject() : username;
    }
}
