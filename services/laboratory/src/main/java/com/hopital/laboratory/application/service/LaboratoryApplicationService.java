package com.hopital.laboratory.application.service;

import com.hopital.laboratory.application.domain.AnalysisRequestStatus;
import com.hopital.laboratory.application.domain.AnalysisRequestEventType;
import com.hopital.laboratory.application.domain.AnalysisResultStatus;
import com.hopital.laboratory.application.domain.AnalysisResultFlag;
import com.hopital.laboratory.application.domain.AnalysisValueType;
import com.hopital.laboratory.application.domain.AnalysisPriority;
import com.hopital.laboratory.application.domain.DataAccessScope;
import com.hopital.laboratory.application.domain.LaboratoryType;
import com.hopital.laboratory.application.domain.SpecimenStatus;
import com.hopital.laboratory.application.dto.AnalysisRequestResponse;
import com.hopital.laboratory.application.dto.AnalysisRequestEventResponse;
import com.hopital.laboratory.application.dto.AnalysisRequestDetailResponse;
import com.hopital.laboratory.application.dto.AnalysisResultResponse;
import com.hopital.laboratory.application.dto.AnalysisDefinitionResponse;
import com.hopital.laboratory.application.dto.AnalysisParameterResponse;
import com.hopital.laboratory.application.dto.AnalysisResultValueResponse;
import com.hopital.laboratory.application.dto.ClinicalInterpretationResponse;
import com.hopital.laboratory.application.dto.CreateAnalysisDefinitionRequest;
import com.hopital.laboratory.application.dto.CreateAnalysisResultValueRequest;
import com.hopital.laboratory.application.dto.CreateClinicalInterpretationRequest;
import com.hopital.laboratory.application.dto.CreateDiseaseRequest;
import com.hopital.laboratory.application.dto.DiseaseResponse;
import com.hopital.laboratory.application.dto.CreateAnalysisRequestRequest;
import com.hopital.laboratory.application.dto.CreateAnalysisResultRequest;
import com.hopital.laboratory.application.dto.CreatePatientPassageAnalysisRequest;
import com.hopital.laboratory.application.dto.CreateReferenceSpecimenCollectionRequest;
import com.hopital.laboratory.application.dto.CreateSpecimenRequest;
import com.hopital.laboratory.application.dto.DispatchReferenceSpecimenRequest;
import com.hopital.laboratory.application.dto.HospitalLaboratoryOptionResponse;
import com.hopital.laboratory.application.dto.ReferenceLaboratoryOptionResponse;
import com.hopital.laboratory.application.dto.PageResponse;
import com.hopital.laboratory.application.dto.PatientPassageLaboratoryRequestResponse;
import com.hopital.laboratory.application.dto.SpecimenResponse;
import com.hopital.laboratory.application.dto.SpecimenDetailResponse;
import com.hopital.laboratory.application.dto.ReceiveReferenceSpecimenRequest;
import com.hopital.laboratory.application.dto.RejectReferenceSpecimenRequest;
import com.hopital.laboratory.application.dto.ValidateAnalysisResultRequest;
import com.hopital.laboratory.application.exception.InvalidLaboratoryWorkflowException;
import com.hopital.laboratory.application.exception.DataAccessDeniedException;
import com.hopital.laboratory.application.exception.LaboratoryResourceNotFoundException;
import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestEntity;
import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestEventEntity;
import com.hopital.laboratory.infra.persistence.entity.AnalysisResultEntity;
import com.hopital.laboratory.infra.persistence.entity.AnalysisDefinitionEntity;
import com.hopital.laboratory.infra.persistence.entity.AnalysisDefinitionParameterEntity;
import com.hopital.laboratory.infra.persistence.entity.AnalysisRequestParameterEntity;
import com.hopital.laboratory.infra.persistence.entity.AnalysisResultInterpretationEntity;
import com.hopital.laboratory.infra.persistence.entity.AnalysisResultValueEntity;
import com.hopital.laboratory.infra.persistence.entity.DiseaseDefinitionEntity;
import com.hopital.laboratory.infra.persistence.entity.SpecimenEntity;
import com.hopital.laboratory.infra.persistence.repository.AnalysisRequestRepository;
import com.hopital.laboratory.infra.persistence.repository.AnalysisRequestEventRepository;
import com.hopital.laboratory.infra.persistence.repository.AnalysisResultRepository;
import com.hopital.laboratory.infra.persistence.repository.AnalysisDefinitionParameterRepository;
import com.hopital.laboratory.infra.persistence.repository.AnalysisDefinitionRepository;
import com.hopital.laboratory.infra.persistence.repository.AnalysisRequestParameterRepository;
import com.hopital.laboratory.infra.persistence.repository.AnalysisResultInterpretationRepository;
import com.hopital.laboratory.infra.persistence.repository.AnalysisResultValueRepository;
import com.hopital.laboratory.infra.persistence.repository.DiseaseDefinitionRepository;
import com.hopital.laboratory.infra.persistence.repository.SpecimenRepository;
import com.hopital.laboratory.infra.integration.organization.HospitalLaboratoryReferenceClient;
import com.hopital.laboratory.infra.integration.patient.PatientPassageReferenceClient;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private final AnalysisRequestEventRepository analysisRequestEventRepository;
    private final SpecimenRepository specimenRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final AnalysisDefinitionRepository analysisDefinitionRepository;
    private final AnalysisDefinitionParameterRepository analysisDefinitionParameterRepository;
    private final AnalysisRequestParameterRepository analysisRequestParameterRepository;
    private final AnalysisResultValueRepository analysisResultValueRepository;
    private final DiseaseDefinitionRepository diseaseDefinitionRepository;
    private final AnalysisResultInterpretationRepository analysisResultInterpretationRepository;
    private final PatientPassageReferenceClient patientPassageReferenceClient;
    private final HospitalLaboratoryReferenceClient hospitalLaboratoryReferenceClient;

    public LaboratoryApplicationService(
            AnalysisRequestRepository analysisRequestRepository,
            AnalysisRequestEventRepository analysisRequestEventRepository,
            SpecimenRepository specimenRepository,
            AnalysisResultRepository analysisResultRepository,
            AnalysisDefinitionRepository analysisDefinitionRepository,
            AnalysisDefinitionParameterRepository analysisDefinitionParameterRepository,
            AnalysisRequestParameterRepository analysisRequestParameterRepository,
            AnalysisResultValueRepository analysisResultValueRepository,
            DiseaseDefinitionRepository diseaseDefinitionRepository,
            AnalysisResultInterpretationRepository analysisResultInterpretationRepository,
            PatientPassageReferenceClient patientPassageReferenceClient,
            HospitalLaboratoryReferenceClient hospitalLaboratoryReferenceClient) {
        this.analysisRequestRepository = analysisRequestRepository;
        this.analysisRequestEventRepository = analysisRequestEventRepository;
        this.specimenRepository = specimenRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.analysisDefinitionRepository = analysisDefinitionRepository;
        this.analysisDefinitionParameterRepository = analysisDefinitionParameterRepository;
        this.analysisRequestParameterRepository = analysisRequestParameterRepository;
        this.analysisResultValueRepository = analysisResultValueRepository;
        this.diseaseDefinitionRepository = diseaseDefinitionRepository;
        this.analysisResultInterpretationRepository = analysisResultInterpretationRepository;
        this.patientPassageReferenceClient = patientPassageReferenceClient;
        this.hospitalLaboratoryReferenceClient = hospitalLaboratoryReferenceClient;
    }

    public List<AnalysisRequestResponse> listAnalysisRequests() {
        return listAnalysisRequests(DataAccessScope.provinceWideScope());
    }

    public List<AnalysisRequestResponse> listAnalysisRequests(DataAccessScope accessScope) {
        return analysisRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(request -> canReadRequest(accessScope, request))
                .map(this::toResponse)
                .toList();
    }

    public PageResponse<AnalysisRequestResponse> searchAnalysisRequests(int page, int size, String query) {
        return searchAnalysisRequests(page, size, query, DataAccessScope.provinceWideScope());
    }

    public PageResponse<AnalysisRequestResponse> searchAnalysisRequests(
            int page, int size, String query, DataAccessScope accessScope) {
        return toPageResponse(analysisRequestRepository.search(
                normalizeSearchFilter(query),
                accessScope.provinceWide(),
                scopedLaboratoryCodes(accessScope),
                accessScope.hospitalId(),
                pageRequest(page, size, "createdAt")), this::toResponse);
    }

    /**
     * Queue used by a reference laboratory to receive samples sent by
     * hospitals. Originating-hospital access is intentionally not considered:
     * it grants visibility on its own patient history, never the right to
     * process a specimen at its destination.
     */
    public PageResponse<AnalysisRequestResponse> searchReferenceReceptionRequests(
            int page, int size, String query, DataAccessScope accessScope) {
        return toPageResponse(analysisRequestRepository.searchReferenceReceptions(
                normalizeSearchFilter(query),
                LaboratoryType.REFERENCE,
                AnalysisRequestStatus.SAMPLE_IN_TRANSIT,
                accessScope.provinceWide(),
                scopedLaboratoryCodes(accessScope),
                pageRequest(page, size, "createdAt")), this::toResponse);
    }

    public AnalysisRequestDetailResponse getAnalysisRequestDetail(String analysisRequestCode) {
        return getAnalysisRequestDetail(analysisRequestCode, DataAccessScope.provinceWideScope());
    }

    public AnalysisRequestDetailResponse getAnalysisRequestDetail(String analysisRequestCode, DataAccessScope accessScope) {
        AnalysisRequestEntity request = findAnalysisRequest(analysisRequestCode);
        assertCanReadRequest(accessScope, request);
        return toAnalysisRequestDetailResponse(request);
    }

    public SpecimenDetailResponse getSpecimenDetail(String specimenCode) {
        return getSpecimenDetail(specimenCode, DataAccessScope.provinceWideScope());
    }

    public SpecimenDetailResponse getSpecimenDetail(String specimenCode, DataAccessScope accessScope) {
        SpecimenEntity specimen = specimenRepository.findByCodeIgnoreCase(normalizeCode(specimenCode))
                .orElseThrow(() -> new LaboratoryResourceNotFoundException("L'échantillon", specimenCode));
        assertCanReadRequest(accessScope, specimen.getAnalysisRequest());
        AnalysisRequestDetailResponse requestDetail = toAnalysisRequestDetailResponse(specimen.getAnalysisRequest());
        return new SpecimenDetailResponse(
                toResponse(specimen),
                requestDetail.request(),
                requestDetail.specimens(),
                requestDetail.result());
    }

    public List<HospitalLaboratoryOptionResponse> listHospitalLaboratoriesForPassage(UUID passageId) {
        return listHospitalLaboratoriesForPassage(passageId, DataAccessScope.provinceWideScope());
    }

    public List<HospitalLaboratoryOptionResponse> listHospitalLaboratoriesForPassage(UUID passageId, DataAccessScope accessScope) {
        PatientPassageReferenceClient.PatientPassageReference passage = patientPassageReferenceClient.resolve(passageId);
        assertCanAccessOriginHospital(accessScope, passage.hospitalId());
        HospitalLaboratoryReferenceClient.HospitalReference hospital = hospitalLaboratoryReferenceClient
                .resolveActiveHospital(passage.hospitalId());
        return hospital.hospitalLaboratories().stream()
                .map(laboratory -> new HospitalLaboratoryOptionResponse(laboratory.code(), laboratory.name()))
                .toList();
    }

    /**
     * The destination catalogue is resolved by the hospital of the passage,
     * not supplied by the browser. It contains only active laboratories from
     * the same province.
     */
    public List<ReferenceLaboratoryOptionResponse> listReferenceLaboratoriesForPassage(UUID passageId) {
        return listReferenceLaboratoriesForPassage(passageId, DataAccessScope.provinceWideScope());
    }

    public List<ReferenceLaboratoryOptionResponse> listReferenceLaboratoriesForPassage(UUID passageId, DataAccessScope accessScope) {
        PatientPassageReferenceClient.PatientPassageReference passage = patientPassageReferenceClient.resolve(passageId);
        assertCanAccessOriginHospital(accessScope, passage.hospitalId());
        return hospitalLaboratoryReferenceClient.listActiveReferenceLaboratories(passage.hospitalId()).stream()
                .map(laboratory -> new ReferenceLaboratoryOptionResponse(laboratory.code(), laboratory.name()))
                .toList();
    }

    public PageResponse<PatientPassageLaboratoryRequestResponse> searchPatientPassageAnalysisRequests(
            UUID passageId,
            int page,
            int size,
            String query,
            AnalysisRequestStatus status) {
        return searchPatientPassageAnalysisRequests(
                passageId, page, size, query, status, DataAccessScope.provinceWideScope());
    }

    public PageResponse<PatientPassageLaboratoryRequestResponse> searchPatientPassageAnalysisRequests(
            UUID passageId,
            int page,
            int size,
            String query,
            AnalysisRequestStatus status,
            DataAccessScope accessScope) {
        Page<AnalysisRequestEntity> requests = analysisRequestRepository.searchByPatientPassageId(
                passageId,
                normalizeSearchFilter(query),
                status,
                accessScope.provinceWide(),
                scopedLaboratoryCodes(accessScope),
                accessScope.hospitalId(),
                pageRequest(page, size, "createdAt"));
        return toPatientPassageRequestPage(requests);
    }

    public List<SpecimenResponse> listSpecimens() {
        return listSpecimens(DataAccessScope.provinceWideScope());
    }

    public List<SpecimenResponse> listSpecimens(DataAccessScope accessScope) {
        return specimenRepository.findAllByOrderByReceivedAtDesc().stream()
                .filter(specimen -> canReadRequest(accessScope, specimen.getAnalysisRequest()))
                .map(this::toResponse)
                .toList();
    }

    public PageResponse<SpecimenResponse> searchSpecimens(int page, int size, String query) {
        return searchSpecimens(page, size, query, DataAccessScope.provinceWideScope());
    }

    public PageResponse<SpecimenResponse> searchSpecimens(int page, int size, String query, DataAccessScope accessScope) {
        return toPageResponse(specimenRepository.search(
                normalizeSearchFilter(query),
                accessScope.provinceWide(),
                scopedLaboratoryCodes(accessScope),
                accessScope.hospitalId(),
                pageRequest(page, size, "receivedAt")), this::toResponse);
    }

    public List<AnalysisResultResponse> listAnalysisResults() {
        return listAnalysisResults(DataAccessScope.provinceWideScope());
    }

    public List<AnalysisResultResponse> listAnalysisResults(DataAccessScope accessScope) {
        return analysisResultRepository.findAllByOrderByEnteredAtDesc().stream()
                .filter(result -> canReadRequest(accessScope, result.getAnalysisRequest()))
                .map(this::toResponse)
                .toList();
    }

    public PageResponse<AnalysisResultResponse> searchAnalysisResults(int page, int size, String query) {
        return searchAnalysisResults(page, size, query, DataAccessScope.provinceWideScope());
    }

    public PageResponse<AnalysisResultResponse> searchAnalysisResults(
            int page, int size, String query, DataAccessScope accessScope) {
        return toPageResponse(analysisResultRepository.search(
                normalizeSearchFilter(query),
                accessScope.provinceWide(),
                scopedLaboratoryCodes(accessScope),
                accessScope.hospitalId(),
                pageRequest(page, size, "enteredAt")), this::toResponse);
    }

    public PageResponse<AnalysisDefinitionResponse> searchAnalysisDefinitions(
            int page, int size, String query, com.hopital.laboratory.application.domain.SpecimenType specimenType,
            Boolean active) {
        Page<AnalysisDefinitionEntity> definitions = analysisDefinitionRepository.search(
                normalizeSearchFilter(query), specimenType, active, alphabeticalPageRequest(page, size, "name"));
        return toPageResponse(definitions, this::toAnalysisDefinitionResponse);
    }

    @Transactional
    public AnalysisDefinitionResponse createAnalysisDefinition(
            CreateAnalysisDefinitionRequest request, String actorUsername) {
        Instant now = Instant.now();
        AnalysisDefinitionEntity definition = analysisDefinitionRepository.save(new AnalysisDefinitionEntity(
                UUID.randomUUID(), generateAnalysisDefinitionCode(), request.name().trim(),
                trimToNull(request.description()), request.specimenType(), now, trimToNull(actorUsername)));
        List<AnalysisDefinitionParameterEntity> parameters = new ArrayList<>();
        for (int index = 0; index < request.parameters().size(); index++) {
            CreateAnalysisDefinitionRequest.Parameter parameter = request.parameters().get(index);
            validateDefinitionParameter(parameter);
            parameters.add(new AnalysisDefinitionParameterEntity(
                    UUID.randomUUID(), definition, generateParameterCode(), parameter.name().trim(),
                    parameter.valueType(), trimToNull(parameter.unit()), trimToNull(parameter.referenceRange()),
                    joinOptions(parameter.qualitativeOptions()), parameter.required(), index + 1));
        }
        analysisDefinitionParameterRepository.saveAll(parameters);
        return toAnalysisDefinitionResponse(definition, parameters);
    }

    public PageResponse<DiseaseResponse> searchDiseases(
            int page, int size, String query, Boolean active) {
        return toPageResponse(diseaseDefinitionRepository.search(
                normalizeSearchFilter(query), active, alphabeticalPageRequest(page, size, "name")), this::toDiseaseResponse);
    }

    @Transactional
    public DiseaseResponse createDisease(CreateDiseaseRequest request, String actorUsername) {
        DiseaseDefinitionEntity disease = diseaseDefinitionRepository.save(new DiseaseDefinitionEntity(
                UUID.randomUUID(), generateDiseaseCode(), request.name().trim(), trimToNull(request.description()),
                trimToNull(request.icdSystem()), trimToNull(request.icdCode()), trimToNull(request.icdUri()),
                Instant.now(), trimToNull(actorUsername)));
        return toDiseaseResponse(disease);
    }

    @Transactional
    public ClinicalInterpretationResponse createClinicalInterpretation(
            String resultCode,
            CreateClinicalInterpretationRequest request,
            String doctorUsername,
            DataAccessScope accessScope) {
        AnalysisResultEntity result = analysisResultRepository.findByCodeIgnoreCase(normalizeCode(resultCode))
                .orElseThrow(() -> new LaboratoryResourceNotFoundException("Le résultat", resultCode));
        assertCanReadRequest(accessScope, result.getAnalysisRequest());
        if (result.getStatus() != AnalysisResultStatus.VALIDATED) {
            throw new InvalidLaboratoryWorkflowException(
                    "L'interprétation médicale ne peut être enregistrée qu'après la validation biologique du résultat.");
        }
        if (analysisResultInterpretationRepository.existsByAnalysisResult_Id(result.getId())) {
            throw new InvalidLaboratoryWorkflowException(
                    "Une interprétation médicale existe déjà pour ce résultat.");
        }
        Set<DiseaseDefinitionEntity> diseases = diseaseDefinitionRepository.findAllByIdInAndActiveTrue(request.diseaseIds());
        if (diseases.size() != request.diseaseIds().size()) {
            throw new InvalidLaboratoryWorkflowException(
                    "Une ou plusieurs maladies sélectionnées sont introuvables ou désactivées.");
        }
        Instant now = Instant.now();
        AnalysisResultInterpretationEntity interpretation = analysisResultInterpretationRepository.save(
                new AnalysisResultInterpretationEntity(
                        UUID.randomUUID(), result, request.clinicalConclusion().trim(),
                        doctorUsername == null || doctorUsername.isBlank() ? "inconnu" : doctorUsername.trim(),
                        now, diseases));
        appendEvent(result.getAnalysisRequest(), AnalysisRequestEventType.CLINICAL_INTERPRETATION_RECORDED,
                interpretation.getDoctorUsername(), "Interprétation médicale enregistrée.", now);
        return toClinicalInterpretationResponse(interpretation);
    }

    @Transactional
    public AnalysisRequestResponse createAnalysisRequest(CreateAnalysisRequestRequest request) {
        return createAnalysisRequest(request, request.requesterName(), DataAccessScope.provinceWideScope());
    }

    /**
     * Legacy generic entry point. A request tied to care must start from a
     * patient passage so that its patient and originating hospital cannot be
     * forged by the caller. It is consequently kept for provincial operators
     * only (for migration or exceptional administrative work).
     */
    @Transactional
    public AnalysisRequestResponse createAnalysisRequest(
            CreateAnalysisRequestRequest request,
            String requesterName,
            DataAccessScope accessScope) {
        if (!accessScope.provinceWide()) {
            throw new DataAccessDeniedException();
        }
        AnalysisRequestEntity analysisRequest = new AnalysisRequestEntity(
                UUID.randomUUID(),
                generateAnalysisRequestCode(),
                request.laboratoryType(),
                normalizeCode(request.laboratoryCode()),
                request.patientReference().trim(),
                request.patientName().trim(),
                generateAnalysisCode(),
                request.analysisName().trim(),
                trimToNull(requesterName),
                Instant.now());
        AnalysisRequestEntity savedRequest = analysisRequestRepository.save(analysisRequest);
        appendEvent(savedRequest, AnalysisRequestEventType.REQUEST_CREATED, trimToNull(requesterName), null, savedRequest.getCreatedAt());
        return toResponse(savedRequest);
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
        return createPatientPassageAnalysisRequest(
                passageId, request, requesterName, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public AnalysisRequestResponse createPatientPassageAnalysisRequest(
            UUID passageId,
            CreatePatientPassageAnalysisRequest request,
            String requesterName,
            DataAccessScope accessScope) {
        PatientPassageReferenceClient.PatientPassageReference passage = resolveOpenPassage(passageId);
        assertCanAccessOriginHospital(accessScope, passage.hospitalId());
        AnalysisDefinitionEntity definition = request.analysisDefinitionId() == null
                ? null
                : analysisDefinitionRepository.findById(request.analysisDefinitionId())
                        .filter(AnalysisDefinitionEntity::isActive)
                        .orElseThrow(() -> new LaboratoryResourceNotFoundException(
                                "L'analyse du catalogue", request.analysisDefinitionId().toString()));
        String analysisName = definition == null ? trimToNull(request.analysisName()) : definition.getName();
        if (analysisName == null) {
            throw new InvalidLaboratoryWorkflowException(
                    "Sélectionnez une analyse active du catalogue et précisez les paramètres attendus.");
        }
        HospitalLaboratoryReferenceClient.HospitalReference hospital = hospitalLaboratoryReferenceClient
                .resolveActiveHospital(passage.hospitalId());
        String laboratoryCode = normalizeCode(request.laboratoryCode());
        if (request.laboratoryType() == LaboratoryType.HOSPITAL) {
            if (!hospital.hospitalLaboratoryCodes().stream().anyMatch(code -> code.equalsIgnoreCase(laboratoryCode))) {
                throw new InvalidLaboratoryWorkflowException(
                        "Le laboratoire interne sélectionné n'est pas actif pour l'hôpital de ce passage.");
            }
        } else if (request.laboratoryType() == LaboratoryType.REFERENCE) {
            boolean available = hospitalLaboratoryReferenceClient.listActiveReferenceLaboratories(passage.hospitalId()).stream()
                    .anyMatch(referenceLaboratory -> referenceLaboratory.code().equalsIgnoreCase(laboratoryCode));
            if (!available) {
                throw new InvalidLaboratoryWorkflowException(
                        "Le laboratoire de référence sélectionné n'est pas actif dans la province de cet hôpital.");
            }
        } else {
            throw new InvalidLaboratoryWorkflowException("Le type de laboratoire est invalide.");
        }
        AnalysisRequestEntity analysisRequest = new AnalysisRequestEntity(
                UUID.randomUUID(),
                generateAnalysisRequestCode(),
                request.laboratoryType(),
                laboratoryCode,
                passage.patientCode(),
                passage.patientName(),
                generateAnalysisCode(),
                analysisName,
                trimToNull(requesterName),
                Instant.now(),
                passage.passageId(),
                passage.hospitalId(),
                passage.hospitalCode(),
                request.priority() == null ? AnalysisPriority.ROUTINE : request.priority(),
                trimToNull(request.clinicalIndication()),
                definition,
                definition == null ? null : definition.getSpecimenType());
        AnalysisRequestEntity savedRequest = analysisRequestRepository.save(analysisRequest);
        if (definition != null) {
            List<AnalysisDefinitionParameterEntity> parameters = analysisDefinitionParameterRepository
                    .findAllByAnalysisDefinition_IdOrderByDisplayOrderAsc(definition.getId());
            if (parameters.isEmpty()) {
                throw new InvalidLaboratoryWorkflowException(
                        "L'analyse sélectionnée ne définit aucun paramètre de résultat.");
            }
            analysisRequestParameterRepository.saveAll(parameters.stream()
                    .map(parameter -> new AnalysisRequestParameterEntity(UUID.randomUUID(), savedRequest, parameter))
                    .toList());
        }
        appendEvent(savedRequest, AnalysisRequestEventType.REQUEST_CREATED, trimToNull(requesterName),
                savedRequest.getLaboratoryType() == LaboratoryType.REFERENCE
                        ? "Demande référée vers le laboratoire " + savedRequest.getLaboratoryCode()
                        : "Demande destinée au laboratoire interne " + savedRequest.getLaboratoryCode(),
                savedRequest.getCreatedAt());
        return toResponse(savedRequest);
    }

    @Transactional
    public SpecimenResponse receiveSpecimen(CreateSpecimenRequest request) {
        return receiveSpecimen(request, null, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public SpecimenResponse receiveSpecimen(CreateSpecimenRequest request, DataAccessScope accessScope) {
        return receiveSpecimen(request, null, accessScope);
    }

    @Transactional
    public SpecimenResponse receiveSpecimen(
            CreateSpecimenRequest request,
            String actorUsername,
            DataAccessScope accessScope) {
        AnalysisRequestEntity analysisRequest = findAnalysisRequest(request.analysisRequestCode());
        assertCanProcessDestination(accessScope, analysisRequest);
        return receiveSpecimenForRequest(analysisRequest, request, actorUsername);
    }

    @Transactional
    public SpecimenResponse receiveSpecimenForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            CreateSpecimenRequest request) {
        return receiveSpecimenForPatientPassage(
                passageId, analysisRequestCode, request, null, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public SpecimenResponse receiveSpecimenForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            CreateSpecimenRequest request,
            DataAccessScope accessScope) {
        return receiveSpecimenForPatientPassage(passageId, analysisRequestCode, request, null, accessScope);
    }

    @Transactional
    public SpecimenResponse receiveSpecimenForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            CreateSpecimenRequest request,
            String actorUsername,
            DataAccessScope accessScope) {
        PatientPassageReferenceClient.PatientPassageReference passage = resolveOpenPassage(passageId);
        assertCanAccessOriginHospital(accessScope, passage.hospitalId());
        if (!normalizeCode(analysisRequestCode).equals(normalizeCode(request.analysisRequestCode()))) {
            throw new InvalidLaboratoryWorkflowException("L'échantillon doit être rattaché à la demande indiquée dans l'URL.");
        }
        AnalysisRequestEntity analysisRequest = findPatientPassageAnalysisRequest(passageId, analysisRequestCode);
        return receiveSpecimenForRequest(analysisRequest, request, actorUsername);
    }

    /**
     * First referral step performed at the originating hospital. The generated
     * ECH code is the label to put on the physical container and on the
     * transfer form.
     */
    @Transactional
    public SpecimenResponse collectReferenceSpecimenForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            CreateReferenceSpecimenCollectionRequest request,
            String actorUsername) {
        return collectReferenceSpecimenForPatientPassage(
                passageId, analysisRequestCode, request, actorUsername, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public SpecimenResponse collectReferenceSpecimenForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            CreateReferenceSpecimenCollectionRequest request,
            String actorUsername,
            DataAccessScope accessScope) {
        PatientPassageReferenceClient.PatientPassageReference passage = resolveOpenPassage(passageId);
        assertCanAccessOriginHospital(accessScope, passage.hospitalId());
        AnalysisRequestEntity analysisRequest = findPatientPassageAnalysisRequest(passageId, analysisRequestCode);
        assertReferenceRequest(analysisRequest);
        if (analysisRequest.getStatus() != AnalysisRequestStatus.REQUESTED
                && analysisRequest.getStatus() != AnalysisRequestStatus.RECOLLECTION_REQUIRED) {
            throw new InvalidLaboratoryWorkflowException(
                    "Un échantillon ne peut être prélevé que pour une demande en attente ou à refaire.");
        }
        assertExpectedSpecimenType(analysisRequest, request.specimenType());
        Instant now = Instant.now();
        SpecimenEntity specimen = SpecimenEntity.collectedForReference(
                UUID.randomUUID(),
                generateSpecimenCode(),
                analysisRequest,
                request.specimenType(),
                request.collectedAt(),
                trimToNull(actorUsername),
                trimToNull(request.collectionNote()));
        analysisRequest.markSampleCollected();
        SpecimenEntity savedSpecimen = specimenRepository.save(specimen);
        appendEvent(analysisRequest, AnalysisRequestEventType.SPECIMEN_COLLECTED, trimToNull(actorUsername),
                "Échantillon " + savedSpecimen.getCode() + " prélevé à l'hôpital.", now);
        return toResponse(savedSpecimen);
    }

    /** Handover to transport. A received specimen can never be dispatched again. */
    @Transactional
    public SpecimenResponse dispatchReferenceSpecimenForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            String specimenCode,
            DispatchReferenceSpecimenRequest request,
            String actorUsername) {
        return dispatchReferenceSpecimenForPatientPassage(
                passageId, analysisRequestCode, specimenCode, request, actorUsername, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public SpecimenResponse dispatchReferenceSpecimenForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            String specimenCode,
            DispatchReferenceSpecimenRequest request,
            String actorUsername,
            DataAccessScope accessScope) {
        PatientPassageReferenceClient.PatientPassageReference passage = resolveOpenPassage(passageId);
        assertCanAccessOriginHospital(accessScope, passage.hospitalId());
        AnalysisRequestEntity analysisRequest = findPatientPassageAnalysisRequest(passageId, analysisRequestCode);
        assertReferenceRequest(analysisRequest);
        SpecimenEntity specimen = findSpecimenForRequest(specimenCode, analysisRequest);
        if (specimen.getStatus() != SpecimenStatus.COLLECTED) {
            throw new InvalidLaboratoryWorkflowException(
                    "Seul un échantillon prélevé peut être remis au transport.");
        }
        if (request.dispatchedAt().isBefore(specimen.getCollectedAt())) {
            throw new InvalidLaboratoryWorkflowException(
                    "La date d'expédition ne peut pas être antérieure au prélèvement.");
        }
        specimen.dispatch(request.dispatchedAt(), trimToNull(actorUsername), trimToNull(request.carrierName()),
                trimToNull(request.dispatchNote()));
        analysisRequest.markSampleInTransit();
        appendEvent(analysisRequest, AnalysisRequestEventType.SPECIMEN_DISPATCHED, trimToNull(actorUsername),
                "Échantillon " + specimen.getCode() + " expédié vers le laboratoire de référence.", request.dispatchedAt());
        return toResponse(specimen);
    }

    /** Physical reception occurs at the destination reference laboratory. */
    @Transactional
    public SpecimenResponse receiveReferenceSpecimen(
            String analysisRequestCode,
            String specimenCode,
            ReceiveReferenceSpecimenRequest request,
            String actorUsername) {
        return receiveReferenceSpecimen(
                analysisRequestCode, specimenCode, request, actorUsername, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public SpecimenResponse receiveReferenceSpecimen(
            String analysisRequestCode,
            String specimenCode,
            ReceiveReferenceSpecimenRequest request,
            String actorUsername,
            DataAccessScope accessScope) {
        AnalysisRequestEntity analysisRequest = findAnalysisRequest(analysisRequestCode);
        assertReferenceRequest(analysisRequest);
        assertCanProcessReferenceRequest(accessScope, analysisRequest);
        SpecimenEntity specimen = findSpecimenForRequest(specimenCode, analysisRequest);
        if (specimen.getStatus() != SpecimenStatus.IN_TRANSIT) {
            throw new InvalidLaboratoryWorkflowException(
                    "Cet échantillon doit être expédié par l'hôpital avant sa réception au laboratoire de référence.");
        }
        if (request.receivedAt().isBefore(specimen.getDispatchedAt())) {
            throw new InvalidLaboratoryWorkflowException(
                    "La date de réception ne peut pas être antérieure à l'expédition.");
        }
        specimen.receive(request.receivedAt(), trimToNull(actorUsername), trimToNull(request.receptionCondition()));
        analysisRequest.markSampleReceived();
        appendEvent(analysisRequest, AnalysisRequestEventType.SPECIMEN_RECEIVED, trimToNull(actorUsername),
                "Échantillon " + specimen.getCode() + " réceptionné et accepté au laboratoire de référence.", request.receivedAt());
        return toResponse(specimen);
    }

    /** A rejection keeps the request traceable and explicitly asks the hospital for a new sample. */
    @Transactional
    public SpecimenResponse rejectReferenceSpecimen(
            String analysisRequestCode,
            String specimenCode,
            RejectReferenceSpecimenRequest request,
            String actorUsername) {
        return rejectReferenceSpecimen(
                analysisRequestCode, specimenCode, request, actorUsername, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public SpecimenResponse rejectReferenceSpecimen(
            String analysisRequestCode,
            String specimenCode,
            RejectReferenceSpecimenRequest request,
            String actorUsername,
            DataAccessScope accessScope) {
        AnalysisRequestEntity analysisRequest = findAnalysisRequest(analysisRequestCode);
        assertReferenceRequest(analysisRequest);
        assertCanProcessReferenceRequest(accessScope, analysisRequest);
        SpecimenEntity specimen = findSpecimenForRequest(specimenCode, analysisRequest);
        if (specimen.getStatus() != SpecimenStatus.IN_TRANSIT) {
            throw new InvalidLaboratoryWorkflowException(
                    "Seul un échantillon en transit peut être refusé à la réception.");
        }
        Instant now = Instant.now();
        specimen.reject(now, trimToNull(actorUsername), request.rejectionReason().trim());
        analysisRequest.markRecollectionRequired();
        appendEvent(analysisRequest, AnalysisRequestEventType.SPECIMEN_REJECTED, trimToNull(actorUsername),
                "Échantillon " + specimen.getCode() + " refusé : " + request.rejectionReason().trim(), now);
        return toResponse(specimen);
    }

    @Transactional
    public AnalysisResultResponse enterAnalysisResult(CreateAnalysisResultRequest request) {
        return enterAnalysisResult(request, null, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public AnalysisResultResponse enterAnalysisResult(
            CreateAnalysisResultRequest request,
            String actorUsername,
            DataAccessScope accessScope) {
        AnalysisRequestEntity analysisRequest = findAnalysisRequest(request.analysisRequestCode());
        assertCanProcessDestination(accessScope, analysisRequest);
        return enterResultForRequest(analysisRequest, request, actorUsername);
    }

    @Transactional
    public AnalysisResultResponse enterAnalysisResultForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            CreateAnalysisResultRequest request) {
        return enterAnalysisResultForPatientPassage(
                passageId, analysisRequestCode, request, null, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public AnalysisResultResponse enterAnalysisResultForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            CreateAnalysisResultRequest request,
            String actorUsername,
            DataAccessScope accessScope) {
        if (!normalizeCode(analysisRequestCode).equals(normalizeCode(request.analysisRequestCode()))) {
            throw new InvalidLaboratoryWorkflowException("Le résultat doit être rattaché à la demande indiquée dans l'URL.");
        }
        AnalysisRequestEntity analysisRequest = findPatientPassageAnalysisRequest(passageId, analysisRequestCode);
        assertCanProcessDestination(accessScope, analysisRequest);
        if (analysisRequest.getLaboratoryType() == LaboratoryType.HOSPITAL) {
            resolveOpenPassage(passageId);
        } else {
            // The reference laboratory can conclude an analysis after the hospital passage is closed.
            patientPassageReferenceClient.resolve(passageId);
        }
        return enterResultForRequest(analysisRequest, request, actorUsername);
    }

    @Transactional
    public AnalysisResultResponse validateAnalysisResult(String resultCode, ValidateAnalysisResultRequest request) {
        return validateAnalysisResult(resultCode, request.validatedBy());
    }

    @Transactional
    public AnalysisResultResponse validateAnalysisResult(String resultCode, String validatedBy) {
        return validateAnalysisResult(resultCode, validatedBy, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public AnalysisResultResponse validateAnalysisResult(
            String resultCode,
            String validatedBy,
            DataAccessScope accessScope) {
        AnalysisResultEntity analysisResult = analysisResultRepository.findByCodeIgnoreCase(normalizeCode(resultCode))
                .orElseThrow(() -> new LaboratoryResourceNotFoundException("Le résultat", resultCode));
        assertCanProcessDestination(accessScope, analysisResult.getAnalysisRequest());
        if (analysisResult.getStatus() == AnalysisResultStatus.VALIDATED) {
            throw new InvalidLaboratoryWorkflowException("Ce résultat a déjà été validé.");
        }
        Instant now = Instant.now();
        String actorUsername = validatedBy == null || validatedBy.isBlank() ? "inconnu" : validatedBy.trim();
        analysisResult.validate(actorUsername, now);
        analysisResult.getAnalysisRequest().markValidated();
        appendEvent(analysisResult.getAnalysisRequest(), AnalysisRequestEventType.RESULT_VALIDATED, actorUsername, null, now);
        return toResponse(analysisResult);
    }

    @Transactional
    public AnalysisResultResponse validateAnalysisResultForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            String resultCode,
            String validatedBy) {
        return validateAnalysisResultForPatientPassage(
                passageId, analysisRequestCode, resultCode, validatedBy, DataAccessScope.provinceWideScope());
    }

    @Transactional
    public AnalysisResultResponse validateAnalysisResultForPatientPassage(
            UUID passageId,
            String analysisRequestCode,
            String resultCode,
            String validatedBy,
            DataAccessScope accessScope) {
        AnalysisRequestEntity analysisRequest = findPatientPassageAnalysisRequest(passageId, analysisRequestCode);
        assertCanProcessDestination(accessScope, analysisRequest);
        if (analysisRequest.getLaboratoryType() == LaboratoryType.HOSPITAL) {
            resolveOpenPassage(passageId);
        } else {
            patientPassageReferenceClient.resolve(passageId);
        }
        AnalysisResultEntity analysisResult = analysisResultRepository.findByCodeIgnoreCase(normalizeCode(resultCode))
                .orElseThrow(() -> new LaboratoryResourceNotFoundException("Le résultat", resultCode));
        if (!analysisResult.getAnalysisRequest().getId().equals(analysisRequest.getId())) {
            throw new LaboratoryResourceNotFoundException("Le résultat", resultCode);
        }
        if (analysisResult.getStatus() == AnalysisResultStatus.VALIDATED) {
            throw new InvalidLaboratoryWorkflowException("Ce résultat a déjà été validé.");
        }
        Instant now = Instant.now();
        String actorUsername = validatedBy == null || validatedBy.isBlank() ? "inconnu" : validatedBy.trim();
        analysisResult.validate(actorUsername, now);
        analysisRequest.markValidated();
        appendEvent(analysisRequest, AnalysisRequestEventType.RESULT_VALIDATED, actorUsername, null, now);
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

    private SpecimenEntity findSpecimenForRequest(String specimenCode, AnalysisRequestEntity analysisRequest) {
        SpecimenEntity specimen = specimenRepository.findByCodeIgnoreCase(normalizeCode(specimenCode))
                .orElseThrow(() -> new LaboratoryResourceNotFoundException("L'échantillon", specimenCode));
        if (!specimen.getAnalysisRequest().getId().equals(analysisRequest.getId())) {
            throw new LaboratoryResourceNotFoundException("L'échantillon", specimenCode);
        }
        return specimen;
    }

    private void assertReferenceRequest(AnalysisRequestEntity analysisRequest) {
        if (analysisRequest.getLaboratoryType() != LaboratoryType.REFERENCE) {
            throw new InvalidLaboratoryWorkflowException(
                    "Cette opération est réservée aux demandes envoyées à un laboratoire de référence.");
        }
    }

    private AnalysisRequestDetailResponse toAnalysisRequestDetailResponse(AnalysisRequestEntity request) {
        List<SpecimenResponse> specimens = specimenRepository
                .findAllByAnalysisRequest_IdInOrderByReceivedAtDesc(List.of(request.getId()))
                .stream()
                .map(this::toResponse)
                .toList();
        AnalysisResultResponse result = analysisResultRepository
                .findAllByAnalysisRequest_IdIn(List.of(request.getId()))
                .stream()
                .findFirst()
                .map(this::toResponse)
                .orElse(null);
        List<AnalysisRequestEventResponse> events = analysisRequestEventRepository
                .findAllByAnalysisRequest_IdOrderByOccurredAtAsc(request.getId())
                .stream()
                .map(event -> new AnalysisRequestEventResponse(
                        event.getType(), event.getActorUsername(), event.getNote(), event.getOccurredAt()))
                .toList();
        ClinicalInterpretationResponse interpretation = result == null ? null : analysisResultInterpretationRepository
                .findByAnalysisResult_Id(result.id())
                .map(this::toClinicalInterpretationResponse)
                .orElse(null);
        return new AnalysisRequestDetailResponse(toResponse(request), specimens, result, events, interpretation);
    }

    private PatientPassageReferenceClient.PatientPassageReference resolveOpenPassage(UUID passageId) {
        PatientPassageReferenceClient.PatientPassageReference passage = patientPassageReferenceClient.resolve(passageId);
        if (!"OPEN".equals(passage.status())) {
            throw new InvalidLaboratoryWorkflowException(
                    "Le passage est terminé ou annulé : aucune nouvelle opération de laboratoire ne peut y être ajoutée.");
        }
        return passage;
    }

    private SpecimenResponse receiveSpecimenForRequest(
            AnalysisRequestEntity analysisRequest,
            CreateSpecimenRequest request,
            String actorUsername) {
        if (analysisRequest.getLaboratoryType() == LaboratoryType.REFERENCE) {
            throw new InvalidLaboratoryWorkflowException(
                    "Une demande référée doit être prélevée puis expédiée par l'hôpital avant sa réception au laboratoire de référence.");
        }
        if (analysisRequest.getStatus() == AnalysisRequestStatus.RESULT_ENTERED
                || analysisRequest.getStatus() == AnalysisRequestStatus.VALIDATED) {
            throw new InvalidLaboratoryWorkflowException(
                    "Un échantillon ne peut plus être ajouté après la saisie ou la validation du résultat.");
        }
        if (analysisRequest.getStatus() != AnalysisRequestStatus.REQUESTED) {
            throw new InvalidLaboratoryWorkflowException(
                    "Cette demande possède déjà un échantillon. Créez une nouvelle demande si une autre analyse est nécessaire.");
        }
        assertExpectedSpecimenType(analysisRequest, request.specimenType());
        String code = generateSpecimenCode();
        Instant receivedAt = Instant.now();
        SpecimenEntity specimen = new SpecimenEntity(
                UUID.randomUUID(), code, analysisRequest, request.specimenType(), request.collectedAt(), receivedAt);
        analysisRequest.markSampleReceived();
        SpecimenEntity savedSpecimen = specimenRepository.save(specimen);
        appendEvent(analysisRequest, AnalysisRequestEventType.SPECIMEN_RECEIVED, trimToNull(actorUsername),
                "Échantillon " + savedSpecimen.getCode() + " réceptionné par le laboratoire interne.", receivedAt);
        return toResponse(savedSpecimen);
    }

    private AnalysisResultResponse enterResultForRequest(
            AnalysisRequestEntity analysisRequest,
            CreateAnalysisResultRequest request,
            String actorUsername) {
        if (analysisRequest.getStatus() != AnalysisRequestStatus.SAMPLE_RECEIVED) {
            throw new InvalidLaboratoryWorkflowException(
                    "Le résultat ne peut être saisi qu'après la réception d'un échantillon.");
        }
        if (analysisResultRepository.existsByAnalysisRequest_Id(analysisRequest.getId())) {
            throw new InvalidLaboratoryWorkflowException("Un résultat existe déjà pour cette demande d'analyse.");
        }
        List<AnalysisRequestParameterEntity> parameters = analysisRequestParameterRepository
                .findAllByAnalysisRequest_IdOrderByDisplayOrderAsc(analysisRequest.getId());
        List<CreateAnalysisResultValueRequest> submittedValues = request.values() == null ? List.of() : request.values();
        if (!parameters.isEmpty()) {
            validateSubmittedResultValues(parameters, submittedValues);
        } else if (trimToNull(request.resultValue()) == null) {
            throw new InvalidLaboratoryWorkflowException("Le résultat de cette analyse doit être renseigné.");
        }
        String code = generateAnalysisResultCode();
        String resultSummary = parameters.isEmpty()
                ? request.resultValue().trim()
                : buildResultSummary(parameters, submittedValues);
        AnalysisResultEntity analysisResult = new AnalysisResultEntity(
                UUID.randomUUID(),
                code,
                analysisRequest,
                resultSummary,
                parameters.isEmpty() ? trimToNull(request.unit()) : null,
                parameters.isEmpty() ? trimToNull(request.referenceRange()) : null,
                trimToNull(request.comment()),
                Instant.now());
        analysisRequest.markResultEntered();
        AnalysisResultEntity savedResult = analysisResultRepository.save(analysisResult);
        if (!parameters.isEmpty()) {
            Map<UUID, AnalysisRequestParameterEntity> parameterById = parameters.stream()
                    .collect(java.util.stream.Collectors.toMap(AnalysisRequestParameterEntity::getId, Function.identity()));
            analysisResultValueRepository.saveAll(submittedValues.stream()
                    .map(value -> toResultValueEntity(savedResult, parameterById.get(value.requestParameterId()), value))
                    .toList());
        }
        appendEvent(analysisRequest, AnalysisRequestEventType.RESULT_ENTERED, trimToNull(actorUsername), null, savedResult.getEnteredAt());
        return toResponse(savedResult);
    }

    private void validateSubmittedResultValues(
            List<AnalysisRequestParameterEntity> parameters,
            List<CreateAnalysisResultValueRequest> values) {
        Map<UUID, AnalysisRequestParameterEntity> parameterById = parameters.stream()
                .collect(java.util.stream.Collectors.toMap(AnalysisRequestParameterEntity::getId, Function.identity()));
        Set<UUID> submittedIds = new java.util.HashSet<>();
        for (CreateAnalysisResultValueRequest value : values) {
            if (!submittedIds.add(value.requestParameterId())) {
                throw new InvalidLaboratoryWorkflowException("Un paramètre de résultat a été saisi plusieurs fois.");
            }
            AnalysisRequestParameterEntity parameter = parameterById.get(value.requestParameterId());
            if (parameter == null) {
                throw new InvalidLaboratoryWorkflowException(
                        "Un paramètre ne fait pas partie de la prescription de cette demande.");
            }
            parseTypedValue(parameter, value.value());
        }
        boolean missingRequired = parameters.stream()
                .anyMatch(parameter -> parameter.isRequired() && !submittedIds.contains(parameter.getId()));
        if (missingRequired) {
            throw new InvalidLaboratoryWorkflowException(
                    "Tous les paramètres obligatoires prescrits par le médecin doivent être renseignés.");
        }
    }

    private AnalysisResultValueEntity toResultValueEntity(
            AnalysisResultEntity result,
            AnalysisRequestParameterEntity parameter,
            CreateAnalysisResultValueRequest request) {
        Object parsed = parseTypedValue(parameter, request.value());
        BigDecimal numericValue = null;
        Long integerValue = null;
        String textValue = null;
        Boolean booleanValue = null;
        String qualitativeValue = null;
        switch (parameter.getValueType()) {
            case DECIMAL, PERCENTAGE -> numericValue = (BigDecimal) parsed;
            case INTEGER -> integerValue = (Long) parsed;
            case TEXT -> textValue = (String) parsed;
            case BOOLEAN -> booleanValue = (Boolean) parsed;
            case QUALITATIVE -> qualitativeValue = (String) parsed;
        }
        return new AnalysisResultValueEntity(
                UUID.randomUUID(), result, parameter, numericValue, integerValue, textValue, booleanValue,
                qualitativeValue,
                request.abnormalFlag() == null ? AnalysisResultFlag.UNKNOWN : request.abnormalFlag(),
                trimToNull(request.comment()));
    }

    private Object parseTypedValue(AnalysisRequestParameterEntity parameter, String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty()) {
            throw new InvalidLaboratoryWorkflowException(
                    "La valeur de " + parameter.getName() + " est obligatoire.");
        }
        try {
            return switch (parameter.getValueType()) {
                case DECIMAL, PERCENTAGE -> new BigDecimal(value.replace(',', '.'));
                case INTEGER -> Long.valueOf(value);
                case TEXT -> value;
                case BOOLEAN -> parseBooleanValue(value, parameter.getName());
                case QUALITATIVE -> validateQualitativeValue(parameter, value);
            };
        } catch (NumberFormatException exception) {
            throw new InvalidLaboratoryWorkflowException(
                    "La valeur de " + parameter.getName() + " doit être numérique.");
        }
    }

    private Boolean parseBooleanValue(String value, String parameterName) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("oui") || value.equals("1")) return true;
        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("non") || value.equals("0")) return false;
        throw new InvalidLaboratoryWorkflowException(
                "La valeur de " + parameterName + " doit être Oui ou Non.");
    }

    private String validateQualitativeValue(AnalysisRequestParameterEntity parameter, String value) {
        List<String> options = splitOptions(parameter.getQualitativeOptions());
        if (!options.isEmpty() && options.stream().noneMatch(option -> option.equalsIgnoreCase(value))) {
            throw new InvalidLaboratoryWorkflowException(
                    "La valeur de " + parameter.getName() + " doit être choisie dans la liste prévue.");
        }
        return value;
    }

    private String buildResultSummary(
            List<AnalysisRequestParameterEntity> parameters,
            List<CreateAnalysisResultValueRequest> values) {
        Map<UUID, String> valueByParameter = values.stream().collect(java.util.stream.Collectors.toMap(
                CreateAnalysisResultValueRequest::requestParameterId,
                CreateAnalysisResultValueRequest::value));
        String summary = parameters.stream()
                .filter(parameter -> valueByParameter.containsKey(parameter.getId()))
                .map(parameter -> parameter.getName() + " : " + valueByParameter.get(parameter.getId()).trim()
                        + (parameter.getUnit() == null ? "" : " " + parameter.getUnit()))
                .collect(java.util.stream.Collectors.joining(" ; "));
        return summary.length() <= 1000 ? summary : summary.substring(0, 997) + "...";
    }

    private AnalysisRequestResponse toResponse(AnalysisRequestEntity analysisRequest) {
        List<AnalysisParameterResponse> parameters = analysisRequestParameterRepository
                .findAllByAnalysisRequest_IdOrderByDisplayOrderAsc(analysisRequest.getId()).stream()
                .map(this::toAnalysisParameterResponse)
                .toList();
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
                analysisRequest.getOriginHospitalId(),
                analysisRequest.getOriginHospitalCode(),
                analysisRequest.getPriority(),
                analysisRequest.getClinicalIndication(),
                analysisRequest.getStatus(),
                analysisRequest.getCreatedAt(),
                analysisRequest.getPatientPassageId(),
                analysisRequest.getAnalysisDefinition() == null ? null : analysisRequest.getAnalysisDefinition().getId(),
                analysisRequest.getRequestedSpecimenType(),
                parameters);
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
                specimen.getCollectedBy(),
                specimen.getCollectionNote(),
                specimen.getDispatchedAt(),
                specimen.getDispatchedBy(),
                specimen.getCarrierName(),
                specimen.getDispatchNote(),
                specimen.getReceivedAt(),
                specimen.getReceivedBy(),
                specimen.getReceptionCondition(),
                specimen.getRejectionReason());
    }

    private AnalysisResultResponse toResponse(AnalysisResultEntity analysisResult) {
        AnalysisRequestEntity analysisRequest = analysisResult.getAnalysisRequest();
        List<AnalysisResultValueResponse> values = analysisResultValueRepository
                .findAllByAnalysisResult_IdOrderByRequestParameter_DisplayOrderAsc(analysisResult.getId()).stream()
                .map(this::toAnalysisResultValueResponse)
                .toList();
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
                analysisResult.getValidatedBy(),
                values);
    }

    private AnalysisDefinitionResponse toAnalysisDefinitionResponse(AnalysisDefinitionEntity definition) {
        return toAnalysisDefinitionResponse(definition, analysisDefinitionParameterRepository
                .findAllByAnalysisDefinition_IdOrderByDisplayOrderAsc(definition.getId()));
    }

    private AnalysisDefinitionResponse toAnalysisDefinitionResponse(
            AnalysisDefinitionEntity definition,
            List<AnalysisDefinitionParameterEntity> parameters) {
        return new AnalysisDefinitionResponse(
                definition.getId(), definition.getCode(), definition.getName(), definition.getDescription(),
                definition.getSpecimenType(), definition.isActive(),
                parameters.stream().map(this::toAnalysisParameterResponse).toList(), definition.getCreatedAt());
    }

    private AnalysisParameterResponse toAnalysisParameterResponse(AnalysisDefinitionParameterEntity parameter) {
        return new AnalysisParameterResponse(
                parameter.getId(), parameter.getCode(), parameter.getName(), parameter.getValueType(),
                parameter.getUnit(), parameter.getReferenceRange(), splitOptions(parameter.getQualitativeOptions()),
                parameter.isRequired(), parameter.getDisplayOrder());
    }

    private AnalysisParameterResponse toAnalysisParameterResponse(AnalysisRequestParameterEntity parameter) {
        return new AnalysisParameterResponse(
                parameter.getId(), parameter.getCode(), parameter.getName(), parameter.getValueType(),
                parameter.getUnit(), parameter.getReferenceRange(), splitOptions(parameter.getQualitativeOptions()),
                parameter.isRequired(), parameter.getDisplayOrder());
    }

    private AnalysisResultValueResponse toAnalysisResultValueResponse(AnalysisResultValueEntity value) {
        AnalysisRequestParameterEntity parameter = value.getRequestParameter();
        return new AnalysisResultValueResponse(
                parameter.getId(), parameter.getCode(), parameter.getName(), parameter.getValueType(),
                formatStoredValue(value), parameter.getUnit(), parameter.getReferenceRange(),
                value.getAbnormalFlag(), value.getComment());
    }

    private String formatStoredValue(AnalysisResultValueEntity value) {
        return switch (value.getRequestParameter().getValueType()) {
            case DECIMAL, PERCENTAGE -> value.getNumericValue().stripTrailingZeros().toPlainString();
            case INTEGER -> value.getIntegerValue().toString();
            case TEXT -> value.getTextValue();
            case BOOLEAN -> Boolean.TRUE.equals(value.getBooleanValue()) ? "Oui" : "Non";
            case QUALITATIVE -> value.getQualitativeValue();
        };
    }

    private DiseaseResponse toDiseaseResponse(DiseaseDefinitionEntity disease) {
        return new DiseaseResponse(
                disease.getId(), disease.getCode(), disease.getName(), disease.getDescription(),
                disease.getIcdSystem(), disease.getIcdCode(), disease.getIcdUri(), disease.isActive(), disease.getCreatedAt());
    }

    private ClinicalInterpretationResponse toClinicalInterpretationResponse(
            AnalysisResultInterpretationEntity interpretation) {
        return new ClinicalInterpretationResponse(
                interpretation.getId(), interpretation.getAnalysisResult().getCode(),
                interpretation.getClinicalConclusion(), interpretation.getDoctorUsername(),
                interpretation.getInterpretedAt(), interpretation.getDiseases().stream()
                        .sorted(java.util.Comparator.comparing(DiseaseDefinitionEntity::getName))
                        .map(this::toDiseaseResponse)
                        .toList());
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

    private String generateSpecimenCode() {
        for (int attempt = 0; attempt < 8; attempt++) {
            String code = "ECH-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
            if (!specimenRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un code unique d'échantillon.");
    }

    private String generateAnalysisResultCode() {
        for (int attempt = 0; attempt < 8; attempt++) {
            String code = "RES-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
            if (!analysisResultRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un code unique de résultat d'analyse.");
    }

    private String generateAnalysisDefinitionCode() {
        for (int attempt = 0; attempt < 8; attempt++) {
            String code = "ANA-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
            if (!analysisDefinitionRepository.existsByCodeIgnoreCase(code)) return code;
        }
        throw new IllegalStateException("Impossible de générer un code unique d'analyse.");
    }

    private String generateParameterCode() {
        return "PAR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
    }

    private String generateDiseaseCode() {
        for (int attempt = 0; attempt < 8; attempt++) {
            String code = "MAL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
            if (!diseaseDefinitionRepository.existsByCodeIgnoreCase(code)) return code;
        }
        throw new IllegalStateException("Impossible de générer un code unique de maladie.");
    }

    private void validateDefinitionParameter(CreateAnalysisDefinitionRequest.Parameter parameter) {
        List<String> options = parameter.qualitativeOptions() == null ? List.of() : parameter.qualitativeOptions().stream()
                .map(String::trim).filter(value -> !value.isEmpty()).distinct().toList();
        if (parameter.valueType() == AnalysisValueType.QUALITATIVE && options.isEmpty()) {
            throw new InvalidLaboratoryWorkflowException(
                    "Un paramètre qualitatif doit proposer au moins une valeur possible.");
        }
        if (parameter.valueType() != AnalysisValueType.QUALITATIVE && !options.isEmpty()) {
            throw new InvalidLaboratoryWorkflowException(
                    "Les choix prédéfinis sont réservés aux paramètres qualitatifs.");
        }
    }

    private String joinOptions(List<String> options) {
        if (options == null) return null;
        String joined = options.stream().map(String::trim).filter(value -> !value.isEmpty()).distinct()
                .collect(java.util.stream.Collectors.joining("\u001F"));
        return joined.isEmpty() ? null : joined;
    }

    private List<String> splitOptions(String options) {
        return options == null || options.isBlank() ? List.of() : List.of(options.split("\u001F", -1));
    }

    private String normalizeSearchFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private PageRequest pageRequest(int page, int size, String sortField) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(sortField).descending());
    }

    private PageRequest alphabeticalPageRequest(int page, int size, String sortField) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(sortField).ascending());
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
                        specimen.getCollectedBy(),
                        specimen.getDispatchedAt(),
                        specimen.getDispatchedBy(),
                        specimen.getCarrierName(),
                        specimen.getReceivedAt(),
                        specimen.getReceivedBy(),
                        specimen.getRejectionReason()))
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
                        result.getValidatedBy(),
                        analysisResultValueRepository
                                .findAllByAnalysisResult_IdOrderByRequestParameter_DisplayOrderAsc(result.getId()).stream()
                                .map(this::toAnalysisResultValueResponse)
                                .toList());
        List<AnalysisParameterResponse> requestedParameters = analysisRequestParameterRepository
                .findAllByAnalysisRequest_IdOrderByDisplayOrderAsc(request.getId()).stream()
                .map(this::toAnalysisParameterResponse)
                .toList();
        return new PatientPassageLaboratoryRequestResponse(
                request.getId(),
                request.getPatientPassageId(),
                request.getCode(),
                request.getLaboratoryType(),
                request.getLaboratoryCode(),
                request.getAnalysisCode(),
                request.getAnalysisName(),
                request.getRequesterName(),
                request.getPriority(),
                request.getClinicalIndication(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getAnalysisDefinition() == null ? null : request.getAnalysisDefinition().getId(),
                request.getRequestedSpecimenType(),
                requestedParameters,
                specimenTimeline,
                resultTimeline);
    }

    private List<String> scopedLaboratoryCodes(DataAccessScope accessScope) {
        return accessScope.laboratoryCodes().isEmpty() ? List.of("_") : List.copyOf(accessScope.laboratoryCodes());
    }

    private boolean canReadRequest(DataAccessScope accessScope, AnalysisRequestEntity request) {
        return accessScope.provinceWide()
                || accessScope.canAccessLaboratory(request.getLaboratoryCode())
                || accessScope.canAccessOriginHospital(request.getOriginHospitalId());
    }

    private void assertCanReadRequest(DataAccessScope accessScope, AnalysisRequestEntity request) {
        if (!canReadRequest(accessScope, request)) {
            throw new DataAccessDeniedException();
        }
    }

    private void assertCanAccessOriginHospital(DataAccessScope accessScope, UUID hospitalId) {
        if (!accessScope.canAccessOriginHospital(hospitalId)) {
            throw new DataAccessDeniedException();
        }
    }

    /**
     * A reference-laboratory technician is restricted to the reference lab
     * code carried by their active personnel assignment. Provincial staff keep
     * their cross-laboratory supervision scope.
     */
    private void assertCanProcessReferenceRequest(DataAccessScope accessScope, AnalysisRequestEntity request) {
        if (!accessScope.canAccessLaboratory(request.getLaboratoryCode())) {
            throw new DataAccessDeniedException();
        }
    }

    private void assertCanProcessDestination(DataAccessScope accessScope, AnalysisRequestEntity request) {
        if (request.getLaboratoryType() == LaboratoryType.REFERENCE) {
            assertCanProcessReferenceRequest(accessScope, request);
            return;
        }
        if (!accessScope.canAccessLaboratory(request.getLaboratoryCode())) {
            throw new DataAccessDeniedException();
        }
    }

    private void assertExpectedSpecimenType(
            AnalysisRequestEntity analysisRequest,
            com.hopital.laboratory.application.domain.SpecimenType specimenType) {
        if (analysisRequest.getRequestedSpecimenType() != null
                && analysisRequest.getRequestedSpecimenType() != specimenType) {
            throw new InvalidLaboratoryWorkflowException(
                    "Cette analyse exige un échantillon de type "
                            + analysisRequest.getRequestedSpecimenType().name() + ".");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void appendEvent(
            AnalysisRequestEntity analysisRequest,
            AnalysisRequestEventType type,
            String actorUsername,
            String note,
            Instant occurredAt) {
        analysisRequestEventRepository.save(new AnalysisRequestEventEntity(
                UUID.randomUUID(), analysisRequest, type, actorUsername, note, occurredAt));
    }
}
