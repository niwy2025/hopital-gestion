package com.hopital.laboratory.application.service;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.AnalysisResultStatus;
import com.hopital.laboratory.application.domain.LaboratoryType;
import com.hopital.laboratory.application.dto.AnalysisRequestResponse;
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
import com.hopital.laboratory.application.exception.DuplicateLaboratoryResourceException;
import com.hopital.laboratory.application.exception.InvalidLaboratoryWorkflowException;
import com.hopital.laboratory.application.exception.LaboratoryResourceNotFoundException;
import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestEntity;
import com.hopital.laboratory.infra.persistence.entity.AnalysisResultEntity;
import com.hopital.laboratory.infra.persistence.entity.SpecimenEntity;
import com.hopital.laboratory.infra.persistence.repository.AnalysisRequestRepository;
import com.hopital.laboratory.infra.persistence.repository.AnalysisResultRepository;
import com.hopital.laboratory.infra.persistence.repository.SpecimenRepository;
import com.hopital.laboratory.infra.integration.organization.HospitalLaboratoryReferenceClient;
import com.hopital.laboratory.infra.integration.patient.PatientPassageReferenceClient;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LaboratoryApplicationService {

    private final AnalysisRequestRepository analysisRequestRepository;
    private final SpecimenRepository specimenRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final PatientPassageReferenceClient patientPassageReferenceClient;
    private final HospitalLaboratoryReferenceClient hospitalLaboratoryReferenceClient;

    public LaboratoryApplicationService(
            AnalysisRequestRepository analysisRequestRepository,
            SpecimenRepository specimenRepository,
            AnalysisResultRepository analysisResultRepository,
            PatientPassageReferenceClient patientPassageReferenceClient,
            HospitalLaboratoryReferenceClient hospitalLaboratoryReferenceClient) {
        this.analysisRequestRepository = analysisRequestRepository;
        this.specimenRepository = specimenRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.patientPassageReferenceClient = patientPassageReferenceClient;
        this.hospitalLaboratoryReferenceClient = hospitalLaboratoryReferenceClient;
    }

    public List<AnalysisRequestResponse> listAnalysisRequests() {
        return analysisRequestRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    public PageResponse<AnalysisRequestResponse> searchAnalysisRequests(int page, int size, String query) {
        return toPageResponse(
                analysisRequestRepository.search(normalizeSearchFilter(query), pageRequest(page, size, "createdAt")), this::toResponse);
    }

    public List<HospitalLaboratoryOptionResponse> listHospitalLaboratoriesForPassage(UUID passageId) {
        PatientPassageReferenceClient.PatientPassageReference passage = patientPassageReferenceClient.resolve(passageId);
        HospitalLaboratoryReferenceClient.HospitalReference hospital = hospitalLaboratoryReferenceClient
                .resolveActiveHospital(passage.hospitalId());
        return hospital.hospitalLaboratories().stream()
                .map(laboratory -> new HospitalLaboratoryOptionResponse(laboratory.code(), laboratory.name()))
                .toList();
    }

    public PageResponse<PatientPassageLaboratoryRequestResponse> searchPatientPassageAnalysisRequests(
            UUID passageId,
            int page,
            int size,
            String query,
            AnalysisRequestStatus status) {
        Page<AnalysisRequestEntity> requests = analysisRequestRepository.searchByPatientPassageId(
                passageId,
                normalizeSearchFilter(query),
                status,
                pageRequest(page, size, "createdAt"));
        return toPatientPassageRequestPage(requests);
    }

    public List<SpecimenResponse> listSpecimens() {
        return specimenRepository.findAllByOrderByReceivedAtDesc().stream().map(this::toResponse).toList();
    }

    public PageResponse<SpecimenResponse> searchSpecimens(int page, int size, String query) {
        return toPageResponse(specimenRepository.search(normalizeSearchFilter(query), pageRequest(page, size, "receivedAt")), this::toResponse);
    }

    public List<AnalysisResultResponse> listAnalysisResults() {
        return analysisResultRepository.findAllByOrderByEnteredAtDesc().stream().map(this::toResponse).toList();
    }

    public PageResponse<AnalysisResultResponse> searchAnalysisResults(int page, int size, String query) {
        return toPageResponse(
                analysisResultRepository.search(normalizeSearchFilter(query), pageRequest(page, size, "enteredAt")), this::toResponse);
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
                request.laboratoryType(),
                normalizeCode(request.laboratoryCode()),
                request.patientReference().trim(),
                request.patientName().trim(),
                normalizeCode(request.analysisCode()),
                request.analysisName().trim(),
                trimToNull(request.requesterName()),
                Instant.now());
        return toResponse(analysisRequestRepository.save(analysisRequest));
    }

    /**
     * Creates a request directly from a patient passage. Patient identity and
     * hospital are resolved by their source services and cannot be supplied by
     * the browser.
     */
    @Transactional
    public AnalysisRequestResponse createPatientPassageAnalysisRequest(
            UUID passageId,
            CreatePatientPassageAnalysisRequest request,
            String requesterName) {
        PatientPassageReferenceClient.PatientPassageReference passage = resolveOpenPassage(passageId);
        HospitalLaboratoryReferenceClient.HospitalReference hospital = hospitalLaboratoryReferenceClient
                .resolveActiveHospital(passage.hospitalId());
        String laboratoryCode = normalizeCode(request.laboratoryCode());
        if (!hospital.hospitalLaboratoryCodes().stream().anyMatch(code -> code.equalsIgnoreCase(laboratoryCode))) {
            throw new InvalidLaboratoryWorkflowException(
                    "Le laboratoire interne sélectionné n'est pas actif pour l'hôpital de ce passage.");
        }
        AnalysisRequestEntity analysisRequest = new AnalysisRequestEntity(
                UUID.randomUUID(),
                generateAnalysisRequestCode(),
                LaboratoryType.HOSPITAL,
                laboratoryCode,
                passage.patientCode(),
                passage.patientName(),
                generateAnalysisCode(),
                request.analysisName().trim(),
                trimToNull(requesterName),
                Instant.now(),
                passage.passageId());
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
    public SpecimenResponse receiveSpecimenForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            CreateSpecimenRequest request) {
        resolveOpenPassage(passageId);
        if (!normalizeCode(analysisRequestCode).equals(normalizeCode(request.analysisRequestCode()))) {
            throw new InvalidLaboratoryWorkflowException("L'échantillon doit être rattaché à la demande indiquée dans l'URL.");
        }
        AnalysisRequestEntity analysisRequest = findPatientPassageAnalysisRequest(passageId, analysisRequestCode);
        return receiveSpecimenForRequest(analysisRequest, request);
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
    public AnalysisResultResponse enterAnalysisResultForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            CreateAnalysisResultRequest request) {
        resolveOpenPassage(passageId);
        if (!normalizeCode(analysisRequestCode).equals(normalizeCode(request.analysisRequestCode()))) {
            throw new InvalidLaboratoryWorkflowException("Le résultat doit être rattaché à la demande indiquée dans l'URL.");
        }
        AnalysisRequestEntity analysisRequest = findPatientPassageAnalysisRequest(passageId, analysisRequestCode);
        return enterResultForRequest(analysisRequest, request);
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

    @Transactional
    public AnalysisResultResponse validateAnalysisResultForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            String resultCode,
            String validatedBy) {
        resolveOpenPassage(passageId);
        AnalysisRequestEntity analysisRequest = findPatientPassageAnalysisRequest(passageId, analysisRequestCode);
        AnalysisResultEntity analysisResult = analysisResultRepository.findByCodeIgnoreCase(normalizeCode(resultCode))
                .orElseThrow(() -> new LaboratoryResourceNotFoundException("Le résultat", resultCode));
        if (!analysisResult.getAnalysisRequest().getId().equals(analysisRequest.getId())) {
            throw new LaboratoryResourceNotFoundException("Le résultat", resultCode);
        }
        if (analysisResult.getStatus() == AnalysisResultStatus.VALIDATED) {
            throw new InvalidLaboratoryWorkflowException("Ce résultat a déjà été validé.");
        }
        analysisResult.validate(validatedBy, Instant.now());
        analysisRequest.markValidated();
        return toResponse(analysisResult);
    }

    private AnalysisRequestEntity findAnalysisRequest(String code) {
        return analysisRequestRepository.findByCodeIgnoreCase(normalizeCode(code))
                .orElseThrow(() -> new LaboratoryResourceNotFoundException("La demande d'analyse", code));
    }

    private AnalysisRequestEntity findPatientPassageAnalysisRequest(UUID passageId, String requestCode) {
        return analysisRequestRepository.findByCodeIgnoreCaseAndPatientPassageId(normalizeCode(requestCode), passageId)
                .orElseThrow(() -> new LaboratoryResourceNotFoundException("La demande d'analyse", requestCode));
    }

    private PatientPassageReferenceClient.PatientPassageReference resolveOpenPassage(UUID passageId) {
        PatientPassageReferenceClient.PatientPassageReference passage = patientPassageReferenceClient.resolve(passageId);
        if (!"OPEN".equals(passage.status())) {
            throw new InvalidLaboratoryWorkflowException(
                    "Le passage est terminé ou annulé : aucune nouvelle opération de laboratoire ne peut y être ajoutée.");
        }
        return passage;
    }

    private SpecimenResponse receiveSpecimenForRequest(AnalysisRequestEntity analysisRequest, CreateSpecimenRequest request) {
        String code = normalizeCode(request.code());
        if (specimenRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateLaboratoryResourceException("L'échantillon", code);
        }
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

    private AnalysisResultResponse enterResultForRequest(AnalysisRequestEntity analysisRequest, CreateAnalysisResultRequest request) {
        String code = normalizeCode(request.code());
        if (analysisResultRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateLaboratoryResourceException("Le résultat", code);
        }
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

    private AnalysisRequestResponse toResponse(AnalysisRequestEntity analysisRequest) {
        return new AnalysisRequestResponse(
                analysisRequest.getId(),
                analysisRequest.getCode(),
                analysisRequest.getLaboratoryType(),
                analysisRequest.getLaboratoryCode(),
                analysisRequest.getPatientReference(),
                analysisRequest.getPatientName(),
                analysisRequest.getAnalysisCode(),
                analysisRequest.getAnalysisName(),
                analysisRequest.getRequesterName(),
                analysisRequest.getStatus(),
                analysisRequest.getCreatedAt(),
                analysisRequest.getPatientPassageId());
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

    private String generateAnalysisRequestCode() {
        for (int attempt = 0; attempt < 8; attempt++) {
            String code = "LAB-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
            if (!analysisRequestRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un code unique de demande d'analyse.");
    }

    private String generateAnalysisCode() {
        return "ANL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private String normalizeSearchFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private PageRequest pageRequest(int page, int size, String sortField) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(sortField).descending());
    }

    private <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private PageResponse<PatientPassageLaboratoryRequestResponse> toPatientPassageRequestPage(
            Page<AnalysisRequestEntity> requestPage) {
        List<AnalysisRequestEntity> requests = requestPage.getContent();
        Map<UUID, List<SpecimenEntity>> specimensByRequest = new HashMap<>();
        Map<UUID, AnalysisResultEntity> resultsByRequest = new HashMap<>();
        if (!requests.isEmpty()) {
            Collection<UUID> requestIds = requests.stream().map(AnalysisRequestEntity::getId).toList();
            for (SpecimenEntity specimen : specimenRepository.findAllByAnalysisRequest_IdInOrderByReceivedAtDesc(requestIds)) {
                specimensByRequest.computeIfAbsent(specimen.getAnalysisRequest().getId(), ignored -> new java.util.ArrayList<>()).add(specimen);
            }
            for (AnalysisResultEntity result : analysisResultRepository.findAllByAnalysisRequest_IdIn(requestIds)) {
                resultsByRequest.put(result.getAnalysisRequest().getId(), result);
            }
        }
        List<PatientPassageLaboratoryRequestResponse> items = requests.stream()
                .map(request -> toPatientPassageRequestResponse(
                        request,
                        specimensByRequest.getOrDefault(request.getId(), List.of()),
                        resultsByRequest.get(request.getId())))
                .toList();
        return new PageResponse<>(items, requestPage.getNumber(), requestPage.getSize(), requestPage.getTotalElements(), requestPage.getTotalPages());
    }

    private PatientPassageLaboratoryRequestResponse toPatientPassageRequestResponse(
            AnalysisRequestEntity request,
            List<SpecimenEntity> specimens,
            AnalysisResultEntity result) {
        List<PatientPassageLaboratoryRequestResponse.SpecimenTimelineResponse> specimenTimeline = specimens.stream()
                .map(specimen -> new PatientPassageLaboratoryRequestResponse.SpecimenTimelineResponse(
                        specimen.getCode(),
                        specimen.getSpecimenType(),
                        specimen.getStatus(),
                        specimen.getCollectedAt(),
                        specimen.getReceivedAt()))
                .toList();
        PatientPassageLaboratoryRequestResponse.ResultTimelineResponse resultTimeline = result == null ? null
                : new PatientPassageLaboratoryRequestResponse.ResultTimelineResponse(
                        result.getCode(),
                        result.getResultValue(),
                        result.getUnit(),
                        result.getReferenceRange(),
                        result.getComment(),
                        result.getStatus(),
                        result.getEnteredAt(),
                        result.getValidatedAt(),
                        result.getValidatedBy());
        return new PatientPassageLaboratoryRequestResponse(
                request.getId(),
                request.getPatientPassageId(),
                request.getCode(),
                request.getLaboratoryCode(),
                request.getAnalysisCode(),
                request.getAnalysisName(),
                request.getRequesterName(),
                request.getStatus(),
                request.getCreatedAt(),
                specimenTimeline,
                resultTimeline);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
