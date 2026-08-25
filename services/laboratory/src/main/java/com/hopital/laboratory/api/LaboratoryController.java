package com.hopital.laboratory.api;

import com.hopital.laboratory.application.dto.AnalysisRequestResponse;
import com.hopital.laboratory.application.dto.AnalysisResultResponse;
import com.hopital.laboratory.application.dto.CreateAnalysisRequestRequest;
import com.hopital.laboratory.application.dto.CreateAnalysisResultRequest;
import com.hopital.laboratory.application.dto.CreateSpecimenRequest;
import com.hopital.laboratory.application.dto.SpecimenResponse;
import com.hopital.laboratory.application.dto.ValidateAnalysisResultRequest;
import com.hopital.laboratory.application.service.LaboratoryApplicationService;
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

    @PostMapping("/analysis-requests")
    public ResponseEntity<AnalysisRequestResponse> createAnalysisRequest(
            @Valid @RequestBody CreateAnalysisRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.createAnalysisRequest(request));
    }

    @GetMapping("/specimens")
    public ResponseEntity<List<SpecimenResponse>> listSpecimens() {
        return ResponseEntity.ok(laboratoryApplicationService.listSpecimens());
    }

    @PostMapping("/specimens")
    public ResponseEntity<SpecimenResponse> receiveSpecimen(@Valid @RequestBody CreateSpecimenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.receiveSpecimen(request));
    }

    @GetMapping("/analysis-results")
    public ResponseEntity<List<AnalysisResultResponse>> listAnalysisResults() {
        return ResponseEntity.ok(laboratoryApplicationService.listAnalysisResults());
    }

    @PostMapping("/analysis-results")
    public ResponseEntity<AnalysisResultResponse> enterAnalysisResult(
            @Valid @RequestBody CreateAnalysisResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryApplicationService.enterAnalysisResult(request));
    }

    @PatchMapping("/analysis-results/{resultCode}/validation")
    public ResponseEntity<AnalysisResultResponse> validateAnalysisResult(
            @PathVariable("resultCode") String resultCode,
            @Valid @RequestBody ValidateAnalysisResultRequest request) {
        return ResponseEntity.ok(laboratoryApplicationService.validateAnalysisResult(resultCode, request));
    }
}
