package com.hopital.laboratory.application.service;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.AnalysisResultStatus;
import com.hopital.laboratory.application.dto.AnalysisRequestResponse;
import com.hopital.laboratory.application.dto.AnalysisResultResponse;
import com.hopital.laboratory.application.dto.CreateAnalysisRequestRequest;
import com.hopital.laboratory.application.dto.CreateAnalysisResultRequest;
import com.hopital.laboratory.application.dto.CreateSpecimenRequest;
import com.hopital.laboratory.application.dto.SpecimenResponse;
import com.hopital.laboratory.application.dto.ValidateAnalysisResultRequest;
import com.hopital.laboratory.application.exception.DuplicateLaboratoryResourceException;
import com.hopital.laboratory.application.exception.InvalidLaboratoryWorkflowException;
import com.hopital.laboratory.application.exception.LaboratoryResourceNotFoundException;
import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestEntity;
import com.hopital.laboratory.infra.persistence.entity.AnalysisResultEntity;
import com.hopital.laboratory.infra.persistence.entity.SpecimenEntity;
import com.hopital.laboratory.infra.persistence.repository.AnalysisRequestRepository;
import com.hopital.laboratory.infra.persistence.repository.AnalysisResultRepository;
import com.hopital.laboratory.infra.persistence.repository.SpecimenRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LaboratoryApplicationService {

    private final AnalysisRequestRepository analysisRequestRepository;
    private final SpecimenRepository specimenRepository;
    private final AnalysisResultRepository analysisResultRepository;

    public LaboratoryApplicationService(
            AnalysisRequestRepository analysisRequestRepository,
            SpecimenRepository specimenRepository,
            AnalysisResultRepository analysisResultRepository) {
        this.analysisRequestRepository = analysisRequestRepository;
        this.specimenRepository = specimenRepository;
        this.analysisResultRepository = analysisResultRepository;
    }

    public List<AnalysisRequestResponse> listAnalysisRequests() {
        return analysisRequestRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    public List<SpecimenResponse> listSpecimens() {
        return specimenRepository.findAllByOrderByReceivedAtDesc().stream().map(this::toResponse).toList();
    }

    public List<AnalysisResultResponse> listAnalysisResults() {
        return analysisResultRepository.findAllByOrderByEnteredAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AnalysisRequestResponse createAnalysisRequest(CreateAnalysisRequestRequest request) {
        String code = normalizeCode(request.code());
        if (analysisRequestRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateLaboratoryResourceException("La demande d'analyse", code);
        }
        AnalysisRequestEntity analysisRequest = new AnalysisRequestEntity(
                UUID.randomUUID(),
                code,
                normalizeCode(request.referenceLaboratoryCode()),
                request.patientReference().trim(),
                request.patientName().trim(),
                normalizeCode(request.analysisCode()),
                request.analysisName().trim(),
                trimToNull(request.requesterName()),
                Instant.now());
        return toResponse(analysisRequestRepository.save(analysisRequest));
    }

    @Transactional
    public SpecimenResponse receiveSpecimen(CreateSpecimenRequest request) {
        String code = normalizeCode(request.code());
        if (specimenRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateLaboratoryResourceException("L'échantillon", code);
        }
        AnalysisRequestEntity analysisRequest = findAnalysisRequest(request.analysisRequestCode());
        if (analysisRequest.getStatus() == AnalysisRequestStatus.RESULT_ENTERED
                || analysisRequest.getStatus() == AnalysisRequestStatus.VALIDATED) {
            throw new InvalidLaboratoryWorkflowException(
                    "Un échantillon ne peut plus être ajouté après la saisie ou la validation du résultat.");
        }
        SpecimenEntity specimen = new SpecimenEntity(
                UUID.randomUUID(), code, analysisRequest, request.specimenType(), request.collectedAt(), Instant.now());
        analysisRequest.markSampleReceived();
        return toResponse(specimenRepository.save(specimen));
    }

    @Transactional
    public AnalysisResultResponse enterAnalysisResult(CreateAnalysisResultRequest request) {
        String code = normalizeCode(request.code());
        if (analysisResultRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateLaboratoryResourceException("Le résultat", code);
        }
        AnalysisRequestEntity analysisRequest = findAnalysisRequest(request.analysisRequestCode());
        if (analysisRequest.getStatus() != AnalysisRequestStatus.SAMPLE_RECEIVED) {
            throw new InvalidLaboratoryWorkflowException(
                    "Le résultat ne peut être saisi qu'après la réception d'un échantillon.");
        }
        if (analysisResultRepository.existsByAnalysisRequest_Id(analysisRequest.getId())) {
            throw new InvalidLaboratoryWorkflowException("Un résultat existe déjà pour cette demande d'analyse.");
        }
        AnalysisResultEntity analysisResult = new AnalysisResultEntity(
                UUID.randomUUID(),
                code,
                analysisRequest,
                request.resultValue().trim(),
                trimToNull(request.unit()),
                trimToNull(request.referenceRange()),
                trimToNull(request.comment()),
                Instant.now());
        analysisRequest.markResultEntered();
        return toResponse(analysisResultRepository.save(analysisResult));
    }

    @Transactional
    public AnalysisResultResponse validateAnalysisResult(String resultCode, ValidateAnalysisResultRequest request) {
        AnalysisResultEntity analysisResult = analysisResultRepository.findByCodeIgnoreCase(normalizeCode(resultCode))
                .orElseThrow(() -> new LaboratoryResourceNotFoundException("Le résultat", resultCode));
        if (analysisResult.getStatus() == AnalysisResultStatus.VALIDATED) {
            throw new InvalidLaboratoryWorkflowException("Ce résultat a déjà été validé.");
        }
        analysisResult.validate(request.validatedBy().trim(), Instant.now());
        analysisResult.getAnalysisRequest().markValidated();
        return toResponse(analysisResult);
    }

    private AnalysisRequestEntity findAnalysisRequest(String code) {
        return analysisRequestRepository.findByCodeIgnoreCase(normalizeCode(code))
                .orElseThrow(() -> new LaboratoryResourceNotFoundException("La demande d'analyse", code));
    }

    private AnalysisRequestResponse toResponse(AnalysisRequestEntity analysisRequest) {
        return new AnalysisRequestResponse(
                analysisRequest.getId(),
                analysisRequest.getCode(),
                analysisRequest.getReferenceLaboratoryCode(),
                analysisRequest.getPatientReference(),
                analysisRequest.getPatientName(),
                analysisRequest.getAnalysisCode(),
                analysisRequest.getAnalysisName(),
                analysisRequest.getRequesterName(),
                analysisRequest.getStatus(),
                analysisRequest.getCreatedAt());
    }

    private SpecimenResponse toResponse(SpecimenEntity specimen) {
        AnalysisRequestEntity analysisRequest = specimen.getAnalysisRequest();
        return new SpecimenResponse(
                specimen.getId(),
                specimen.getCode(),
                analysisRequest.getCode(),
                analysisRequest.getPatientName(),
                specimen.getSpecimenType(),
                specimen.getStatus(),
                specimen.getCollectedAt(),
                specimen.getReceivedAt());
    }

    private AnalysisResultResponse toResponse(AnalysisResultEntity analysisResult) {
        AnalysisRequestEntity analysisRequest = analysisResult.getAnalysisRequest();
        return new AnalysisResultResponse(
                analysisResult.getId(),
                analysisResult.getCode(),
                analysisRequest.getCode(),
                analysisRequest.getPatientName(),
                analysisRequest.getAnalysisName(),
                analysisResult.getResultValue(),
                analysisResult.getUnit(),
                analysisResult.getReferenceRange(),
                analysisResult.getComment(),
                analysisResult.getStatus(),
                analysisResult.getEnteredAt(),
                analysisResult.getValidatedAt(),
                analysisResult.getValidatedBy());
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
