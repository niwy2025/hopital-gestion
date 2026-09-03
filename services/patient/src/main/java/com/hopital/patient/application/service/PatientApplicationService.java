package com.hopital.patient.application.service;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.ClinicalEntryType;
import com.hopital.patient.application.domain.ClinicalOrientation;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.Gender;
import com.hopital.patient.application.domain.PatientAuditEventType;
import com.hopital.patient.application.domain.PatientDocumentType;
import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import com.hopital.patient.application.domain.PrescriptionSource;
import com.hopital.patient.application.domain.PrescriptionStatus;
import com.hopital.patient.application.domain.PrescriptionDispenseCompletion;
import com.hopital.patient.application.dto.CreatePrescriptionDispenseRequest;
import com.hopital.patient.application.dto.CreatePharmacyExternalPrescriptionRequest;
import com.hopital.patient.application.dto.CreatePatientPassagePrescriptionRequest;
import com.hopital.patient.application.dto.CreatePatientRequest;
import com.hopital.patient.application.dto.CreatePatientDocumentRequest;
import com.hopital.patient.application.dto.CreatePatientPassageRequest;
import com.hopital.patient.application.dto.CreatePatientPassageClinicalEntryRequest;
import com.hopital.patient.application.dto.AssignPatientPassageResponsiblePersonnelRequest;
import com.hopital.patient.application.dto.EmergencyContactResponse;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.PatientDuplicateCheckRequest;
import com.hopital.patient.application.dto.PatientDuplicateCheckResponse;
import com.hopital.patient.application.dto.PatientAuditEventResponse;
import com.hopital.patient.application.dto.PatientDocumentResponse;
import com.hopital.patient.application.dto.PatientResponse;
import com.hopital.patient.application.dto.PatientPassageResponse;
import com.hopital.patient.application.dto.PatientPassageSummaryResponse;
import com.hopital.patient.application.dto.PatientPassageClinicalEntryResponse;
import com.hopital.patient.application.dto.PatientPassageLaboratoryReferenceResponse;
import com.hopital.patient.application.dto.PatientPassagePrescriptionResponse;
import com.hopital.patient.application.dto.PharmacyPrescriptionResponse;
import com.hopital.patient.application.dto.PharmacyDispenseAccountingLineResponse;
import com.hopital.patient.application.dto.PharmacyDispenseAccountingReferenceResponse;
import com.hopital.patient.application.dto.PrescriptionDispenseItemResponse;
import com.hopital.patient.application.dto.PrescriptionDispenseResponse;
import com.hopital.patient.application.dto.PrescriptionItemResponse;
import com.hopital.patient.application.dto.PatientSummaryResponse;
import com.hopital.patient.application.dto.UpdatePatientStatusRequest;
import com.hopital.patient.application.dto.UpdatePatientRequest;
import com.hopital.patient.application.dto.UpdatePatientPassageStatusRequest;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.exception.DuplicatePatientException;
import com.hopital.patient.application.exception.InvalidPatientDocumentException;
import com.hopital.patient.application.exception.InvalidPatientPassageStateException;
import com.hopital.patient.application.exception.InvalidPrescriptionException;
import com.hopital.patient.application.exception.PatientNotFoundException;
import com.hopital.patient.application.exception.PrescriptionDispenseNotFoundException;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.integration.pharmacy.PharmacyDispenseClient;
import com.hopital.patient.infra.integration.personnel.PersonnelReferenceClient;
import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.infra.persistence.entity.PatientDocumentEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassageEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassageClinicalEntryEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionDispenseEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionDispenseItemEntity;
import com.hopital.patient.infra.persistence.entity.PatientPassagePrescriptionItemEntity;
import com.hopital.patient.infra.persistence.repository.PatientPassageRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassageClinicalEntryRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassagePrescriptionItemRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassagePrescriptionRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassagePrescriptionDispenseItemRepository;
import com.hopital.patient.infra.persistence.repository.PatientPassagePrescriptionDispenseRepository;
import com.hopital.patient.infra.persistence.repository.PatientDocumentRepository;
import com.hopital.patient.infra.persistence.repository.PatientRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PatientApplicationService {

    private static final int MAX_DOCUMENT_SIZE_BYTES = 2 * 1024 * 1024;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> DOCUMENT_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png",
            "image/webp");

    private final PatientRepository patientRepository;
    private final PatientDocumentRepository patientDocumentRepository;
    private final PatientPassageRepository patientPassageRepository;
    private final PatientPassageClinicalEntryRepository patientPassageClinicalEntryRepository;
    private final PatientPassagePrescriptionRepository patientPassagePrescriptionRepository;
    private final PatientPassagePrescriptionItemRepository patientPassagePrescriptionItemRepository;
    private final PatientPassagePrescriptionDispenseRepository patientPassagePrescriptionDispenseRepository;
    private final PatientPassagePrescriptionDispenseItemRepository patientPassagePrescriptionDispenseItemRepository;
    private final HospitalReferenceClient hospitalReferenceClient;
    private final PersonnelReferenceClient personnelReferenceClient;
    private final PharmacyDispenseClient pharmacyDispenseClient;
    private final PharmacyDispenseAccountingOutboxService pharmacyDispenseAccountingOutboxService;

    public PatientApplicationService(
            PatientRepository patientRepository,
            PatientDocumentRepository patientDocumentRepository,
            PatientPassageRepository patientPassageRepository,
            PatientPassageClinicalEntryRepository patientPassageClinicalEntryRepository,
            PatientPassagePrescriptionRepository patientPassagePrescriptionRepository,
            PatientPassagePrescriptionItemRepository patientPassagePrescriptionItemRepository,
            PatientPassagePrescriptionDispenseRepository patientPassagePrescriptionDispenseRepository,
            PatientPassagePrescriptionDispenseItemRepository patientPassagePrescriptionDispenseItemRepository,
            HospitalReferenceClient hospitalReferenceClient,
            PersonnelReferenceClient personnelReferenceClient,
            PharmacyDispenseClient pharmacyDispenseClient,
            PharmacyDispenseAccountingOutboxService pharmacyDispenseAccountingOutboxService) {
        this.patientRepository = patientRepository;
        this.patientDocumentRepository = patientDocumentRepository;
        this.patientPassageRepository = patientPassageRepository;
        this.patientPassageClinicalEntryRepository = patientPassageClinicalEntryRepository;
        this.patientPassagePrescriptionRepository = patientPassagePrescriptionRepository;
        this.patientPassagePrescriptionItemRepository = patientPassagePrescriptionItemRepository;
        this.patientPassagePrescriptionDispenseRepository = patientPassagePrescriptionDispenseRepository;
        this.patientPassagePrescriptionDispenseItemRepository = patientPassagePrescriptionDispenseItemRepository;
        this.hospitalReferenceClient = hospitalReferenceClient;
        this.personnelReferenceClient = personnelReferenceClient;
        this.pharmacyDispenseClient = pharmacyDispenseClient;
        this.pharmacyDispenseAccountingOutboxService = pharmacyDispenseAccountingOutboxService;
    }

    /** Compatibility endpoint for existing dependent forms. New registry screens use the paginated search endpoint. */
    public List<PatientSummaryResponse> listPatients(DataAccessScope accessScope) {
        List<PatientEntity> patients = accessScope.provinceWide()
                ? patientRepository.findAllByOrderByLastNameAscFirstNameAsc()
                : patientRepository.findAllByRegistrationHospitalCodeIgnoreCaseOrderByLastNameAscFirstNameAsc(
                        accessScope.hospitalCode());
        return patients.stream().map(this::toSummary).toList();
    }

    public PageResponse<PatientSummaryResponse> searchPatients(
            int page,
            int size,
            String query,
            UUID hospitalId,
            Boolean active,
            DataAccessScope accessScope) {
        var patients = patientRepository.search(
                normalizeSearchFilter(query),
                accessScope.provinceWide() ? "" : accessScope.hospitalCode(),
                accessScope.provinceWide() ? hospitalId : null,
                active,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("lastName").ascending().and(Sort.by("firstName").ascending())));
        return new PageResponse<>(
                patients.getContent().stream().map(this::toSummary).toList(),
                patients.getNumber(),
                patients.getSize(),
                patients.getTotalElements(),
                patients.getTotalPages());
    }

    public PatientResponse getPatient(UUID patientId, DataAccessScope accessScope) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        return toDetails(patient);
    }

    public PageResponse<PatientPassageResponse> searchPassages(
            UUID patientId,
            int page,
            int size,
            String query,
            PatientPassageType type,
            PatientPassageStatus status,
            DataAccessScope accessScope) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        var passages = patientPassageRepository.search(
                patientId,
                normalizeSearchFilter(query),
                type,
                status,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("arrivedAt").descending()));
        return new PageResponse<>(
                passages.getContent().stream().map(this::toPassage).toList(),
                passages.getNumber(),
                passages.getSize(),
                passages.getTotalElements(),
                passages.getTotalPages());
    }

    /**
     * Documents are loaded separately from the patient dossier so base64 file contents never bloat
     * the administrative record or the patient register.
     */
    public PageResponse<PatientDocumentResponse> searchDocuments(
            UUID patientId,
            int page,
            int size,
            String query,
            PatientDocumentType documentType,
            DataAccessScope accessScope) {
        PatientEntity patient = getPatientForScope(patientId, accessScope);
        var documents = patientDocumentRepository.search(
                patient.getId(),
                normalizeSearchFilter(query),
                documentType,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("createdAt").descending()));
        return new PageResponse<>(
                documents.getContent().stream().map(this::toDocument).toList(),
                documents.getNumber(),
                documents.getSize(),
                documents.getTotalElements(),
                documents.getTotalPages());
    }

    public PageResponse<PatientPassageSummaryResponse> searchPassageRegistry(
            int page,
            int size,
            String query,
            UUID hospitalId,
            PatientPassageType type,
            PatientPassageStatus status,
            boolean assignedToMe,
            DataAccessScope accessScope) {
        String scopeHospitalCode = "";
        if (!accessScope.provinceWide()) {
            if (accessScope.hospitalCode() == null || accessScope.hospitalCode().isBlank()) {
                throw new DataAccessDeniedException();
            }
            scopeHospitalCode = accessScope.hospitalCode();
        }

        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by("arrivedAt").descending());
        if (assignedToMe && accessScope.personnelId() == null) {
            return new PageResponse<>(List.of(), pageRequest.getPageNumber(), pageRequest.getPageSize(), 0, 0);
        }

        var passages = patientPassageRepository.searchRegistry(
                scopeHospitalCode,
                accessScope.provinceWide() ? hospitalId : null,
                normalizeSearchFilter(query),
                type,
                status,
                assignedToMe ? accessScope.personnelId() : null,
                pageRequest);
        return new PageResponse<>(
                passages.getContent().stream()
                        .map(passage -> toPassageSummary(passage, assignedToMe && canManagePassageStatus(accessScope, passage)))
                        .toList(),
                passages.getNumber(),
                passages.getSize(),
                passages.getTotalElements(),
                passages.getTotalPages());
    }

    /**
     * Returns the administrative view of one passage. The passage identifier is
     * globally unique, but access is still constrained to the caller's hospital
     * (or to the whole province for authorized administrators).
     */
    public PatientPassageSummaryResponse getPassage(UUID passageId, DataAccessScope accessScope) {
        PatientPassageEntity passage = patientPassageRepository.findById(passageId)
                .orElseThrow(() -> new PatientNotFoundException(passageId.toString()));
        assertAccess(accessScope, passage.getPatient().getRegistrationHospitalCode());
        return toPassageSummary(passage, canManagePassageStatus(accessScope, passage));
    }

    /**
     * Returns only the data required to bind a laboratory request to a care
     * episode. This method is called through the private container network.
     */
    public PatientPassageLaboratoryReferenceResponse resolvePassageForLaboratory(UUID passageId) {
        PatientPassageEntity passage = patientPassageRepository.findById(passageId)
                .orElseThrow(() -> new PatientNotFoundException(passageId.toString()));
        PatientEntity patient = passage.getPatient();
        String patientName = java.util.stream.Stream.of(patient.getLastName(), patient.getFirstName(), patient.getMiddleName())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
        return new PatientPassageLaboratoryReferenceResponse(
                passage.getId(),
                passage.getCode(),
                patient.getId(),
                patient.getCode(),
                patientName,
                passage.getHospitalId(),
                passage.getHospitalCode(),
                passage.getServiceName(),
                passage.getStatus());
    }

    /**
     * Returns the chronological journal for one care episode without exposing entries from another passage.
     */
    public PageResponse<PatientPassageClinicalEntryResponse> searchClinicalEntries(
            UUID patientId,
            UUID passageId,
            int page,
            int size,
            String query,
            ClinicalEntryType entryType,
            ClinicalOrientation orientation,
            DataAccessScope accessScope) {
        PatientPassageEntity passage = getPassageForPatientScope(patientId, passageId, accessScope);
        var entries = patientPassageClinicalEntryRepository.search(
                passage.getId(),
                normalizeSearchFilter(query),
                entryType,
                orientation,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("recordedAt").descending()));
        return new PageResponse<>(
                entries.getContent().stream().map(this::toClinicalEntry).toList(),
                entries.getNumber(),
                entries.getSize(),
                entries.getTotalElements(),
                entries.getTotalPages());
    }

    /**
     * Returns the orders for one care episode. Medication lines are loaded only
     * for the current page, so a long patient history stays paginated.
     */
    public PageResponse<PatientPassagePrescriptionResponse> searchPrescriptions(
            UUID patientId,
            UUID passageId,
            int page,
            int size,
            String query,
            PrescriptionSource source,
            DataAccessScope accessScope) {
        PatientPassageEntity passage = getPassageForPatientScope(patientId, passageId, accessScope);
        var prescriptions = patientPassagePrescriptionRepository.search(
                passage.getId(),
                normalizeSearchFilter(query),
                source,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("createdAt").descending()));
        List<UUID> prescriptionIds = prescriptions.getContent().stream()
                .map(PatientPassagePrescriptionEntity::getId)
                .toList();
        Map<UUID, List<PatientPassagePrescriptionItemEntity>> itemsByPrescription = new HashMap<>();
        if (!prescriptionIds.isEmpty()) {
            for (PatientPassagePrescriptionItemEntity item : patientPassagePrescriptionItemRepository
                    .findAllByPrescription_IdInOrderByDisplayOrderAsc(prescriptionIds)) {
                itemsByPrescription.computeIfAbsent(item.getPrescription().getId(), ignored -> new ArrayList<>()).add(item);
            }
        }
        return new PageResponse<>(
                prescriptions.getContent().stream()
                        .map(prescription -> toPrescription(
                                prescription,
                                itemsByPrescription.getOrDefault(prescription.getId(), List.of())))
                        .toList(),
                prescriptions.getNumber(),
                prescriptions.getSize(),
                prescriptions.getTotalElements(),
                prescriptions.getTotalPages());
    }

    /**
     * Pharmacy work queue. A pharmacist only receives prescriptions for the
     * hospital attached to their account; provincial administrators can see the
     * province-wide queue.
     */
    public PageResponse<PharmacyPrescriptionResponse> searchPharmacyPrescriptions(
            int page,
            int size,
            String query,
            PrescriptionSource source,
            PrescriptionStatus status,
            DataAccessScope accessScope) {
        String hospitalCode = accessScope.provinceWide() ? "" : requiredHospitalCode(accessScope);
        var prescriptions = patientPassagePrescriptionRepository.searchForPharmacy(
                hospitalCode,
                normalizeSearchFilter(query),
                source,
                status,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("createdAt").descending()));
        return new PageResponse<>(
                toPharmacyPrescriptions(prescriptions.getContent()),
                prescriptions.getNumber(),
                prescriptions.getSize(),
                prescriptions.getTotalElements(),
                prescriptions.getTotalPages());
    }

    public PharmacyPrescriptionResponse getPharmacyPrescription(
            UUID prescriptionId,
            DataAccessScope accessScope) {
        PatientPassagePrescriptionEntity prescription = getPharmacyPrescriptionForScope(prescriptionId, accessScope);
        return toPharmacyPrescriptions(List.of(prescription)).getFirst();
    }

    /**
     * Exposes the payment and care context of a single immutable pharmacy
     * delivery to the future accounting service. It is intentionally available
     * only through the internal controller, never through the public gateway.
     */
    public PharmacyDispenseAccountingReferenceResponse resolvePharmacyDispenseAccountingReference(
            String dispenseCode) {
        PatientPassagePrescriptionDispenseEntity dispense = patientPassagePrescriptionDispenseRepository
                .findByCodeIgnoreCase(dispenseCode)
                .orElseThrow(() -> new PrescriptionDispenseNotFoundException(dispenseCode));
        List<PatientPassagePrescriptionDispenseItemEntity> dispenseItems = patientPassagePrescriptionDispenseItemRepository
                .findAllByDispense_IdInOrderByDispense_IdAsc(List.of(dispense.getId()));
        PatientPassagePrescriptionEntity prescription = dispense.getPrescription();
        PatientPassageEntity passage = prescription.getPassage();
        PatientEntity patient = passage.getPatient();
        return new PharmacyDispenseAccountingReferenceResponse(
                dispense.getId(),
                dispense.getCode(),
                passage.getHospitalId(),
                passage.getHospitalCode(),
                patient.getId(),
                patient.getCode(),
                passage.getId(),
                passage.getCode(),
                prescription.getId(),
                prescription.getCode(),
                prescription.getSource(),
                dispense.getCompletion(),
                dispense.getTotalAmount(),
                dispense.getPaidAmount(),
                dispense.getCurrency(),
                dispense.getPaymentMethod(),
                dispense.getDispensedAt(),
                dispense.getDispensedByUserId(),
                dispense.getDispensedByUsername(),
                dispenseItems.stream()
                        .map(item -> new PharmacyDispenseAccountingLineResponse(
                                item.getPrescriptionItem().getId(),
                                item.getPrescriptionItem().getMedicineId(),
                                item.getPrescriptionItem().getMedicineName(),
                                item.getPrescriptionItem().getDosage(),
                                item.getDispensedQuantity()))
                        .toList());
    }

    @Transactional
    public PrescriptionDispenseResponse dispensePrescription(
            UUID prescriptionId,
            CreatePrescriptionDispenseRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientPassagePrescriptionEntity prescription = getPharmacyPrescriptionForScope(prescriptionId, accessScope);
        if (prescription.getStatus() == PrescriptionStatus.DISPENSED
                || prescription.getStatus() == PrescriptionStatus.CANCELLED) {
            throw new InvalidPrescriptionException("Cette ordonnance est déjà clôturée et ne peut plus être délivrée.");
        }
        if (request.paidAmount() == null || request.paidAmount().signum() < 0
                || request.currency() == null || request.paymentMethod() == null) {
            throw new InvalidPrescriptionException("Le montant, la devise et le mode de paiement sont obligatoires.");
        }

        List<PatientPassagePrescriptionItemEntity> prescriptionItems = patientPassagePrescriptionItemRepository
                .findAllByPrescription_IdInOrderByDisplayOrderAsc(List.of(prescription.getId()));
        if (prescriptionItems.isEmpty()) {
            throw new InvalidPrescriptionException("Cette ordonnance ne contient aucun médicament à délivrer.");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidPrescriptionException("Sélectionnez au moins un médicament délivré.");
        }

        Map<UUID, PatientPassagePrescriptionItemEntity> prescriptionItemsById = new HashMap<>();
        for (PatientPassagePrescriptionItemEntity prescriptionItem : prescriptionItems) {
            prescriptionItemsById.put(prescriptionItem.getId(), prescriptionItem);
        }
        List<PatientPassagePrescriptionDispenseEntity> previousDispenses = patientPassagePrescriptionDispenseRepository
                .findAllByPrescription_IdInOrderByDispensedAtDesc(List.of(prescription.getId()));
        Set<UUID> alreadyDispensedItemIds = new HashSet<>();
        if (!previousDispenses.isEmpty()) {
            List<UUID> previousDispenseIds = previousDispenses.stream()
                    .map(PatientPassagePrescriptionDispenseEntity::getId)
                    .toList();
            patientPassagePrescriptionDispenseItemRepository
                    .findAllByDispense_IdInOrderByDispense_IdAsc(previousDispenseIds)
                    .forEach(item -> alreadyDispensedItemIds.add(item.getPrescriptionItem().getId()));
        }
        Set<UUID> selectedItemIds = new HashSet<>();
        List<PatientPassagePrescriptionItemEntity> selectedPrescriptionItems = new ArrayList<>();
        Map<UUID, String> dispensedQuantitiesByItemId = new HashMap<>();
        for (var itemRequest : request.items()) {
            if (itemRequest.prescriptionItemId() == null || !selectedItemIds.add(itemRequest.prescriptionItemId())) {
                throw new InvalidPrescriptionException("Un médicament ne peut être délivré qu'une fois dans la même opération.");
            }
            PatientPassagePrescriptionItemEntity prescriptionItem = prescriptionItemsById.get(itemRequest.prescriptionItemId());
            if (prescriptionItem == null) {
                throw new InvalidPrescriptionException("Un médicament sélectionné n'appartient pas à cette ordonnance.");
            }
            if (alreadyDispensedItemIds.contains(prescriptionItem.getId())) {
                throw new InvalidPrescriptionException("Ce médicament a déjà été délivré pour cette ordonnance.");
            }
            String dispensedQuantity = trimToNull(itemRequest.dispensedQuantity());
            if (dispensedQuantity == null) {
                throw new InvalidPrescriptionException("La quantité réellement délivrée est obligatoire.");
            }
            selectedPrescriptionItems.add(prescriptionItem);
            dispensedQuantitiesByItemId.put(prescriptionItem.getId(), dispensedQuantity);
        }
        int remainingItemCount = prescriptionItems.size() - alreadyDispensedItemIds.size();
        if (request.complete() && selectedItemIds.size() != remainingItemCount) {
            throw new InvalidPrescriptionException(
                    "Une délivrance complète doit inclure tous les médicaments restant à remettre.");
        }
        if (!request.complete() && selectedItemIds.size() == remainingItemCount) {
            throw new InvalidPrescriptionException(
                    "Tous les médicaments restants sont sélectionnés : enregistrez une délivrance complète.");
        }

        String dispenseCode = nextDispenseCode();
        List<PharmacyDispenseClient.StockDispenseItem> linkedStockItems = selectedPrescriptionItems.stream()
                .filter(item -> item.getMedicineId() != null)
                .map(item -> new PharmacyDispenseClient.StockDispenseItem(
                        item.getMedicineId(),
                        stockQuantity(item, dispensedQuantitiesByItemId.get(item.getId()))))
                .toList();
        PharmacyDispenseClient.DispenseValuation dispenseValuation;
        try {
            dispenseValuation = pharmacyDispenseClient.recordDispense(
                    prescription.getPassage().getHospitalId(),
                    dispenseCode,
                    auditActor,
                    request.paidAmount(),
                    request.currency(),
                    linkedStockItems);
        } catch (RuntimeException exception) {
            throw new InvalidPrescriptionException(
                    "La délivrance ne peut pas être confirmée : le stock disponible doit être vérifié par la pharmacie.");
        }
        if (dispenseValuation.totalAmount() == null || dispenseValuation.totalAmount().signum() < 0) {
            throw new InvalidPrescriptionException("Le montant facturé par la pharmacie est invalide.");
        }
        if (dispenseValuation.currency() != null
                && !request.currency().name().equalsIgnoreCase(dispenseValuation.currency())) {
            throw new InvalidPrescriptionException(
                    "La devise du paiement doit correspondre à la devise du stock délivré.");
        }
        if (request.paidAmount().compareTo(dispenseValuation.totalAmount()) > 0) {
            throw new InvalidPrescriptionException(
                    "Le montant encaissé ne peut pas dépasser le montant facturé par la pharmacie.");
        }

        Instant dispensedAt = Instant.now();
        PatientPassagePrescriptionDispenseEntity dispense = patientPassagePrescriptionDispenseRepository.save(
                new PatientPassagePrescriptionDispenseEntity(
                        UUID.randomUUID(),
                        dispenseCode,
                        prescription,
                        request.complete()
                                ? PrescriptionDispenseCompletion.COMPLETE
                                : PrescriptionDispenseCompletion.PARTIAL,
                        dispenseValuation.totalAmount(),
                        request.paidAmount(),
                        request.currency(),
                        request.paymentMethod(),
                        trimToNull(request.notes()),
                        auditActor,
                        dispensedAt));
        List<PatientPassagePrescriptionDispenseItemEntity> dispenseItems = selectedPrescriptionItems.stream()
                .map(item -> new PatientPassagePrescriptionDispenseItemEntity(
                        UUID.randomUUID(), dispense, item, dispensedQuantitiesByItemId.get(item.getId())))
                .toList();

        List<PatientPassagePrescriptionDispenseItemEntity> savedItems = patientPassagePrescriptionDispenseItemRepository
                .saveAll(dispenseItems);
        prescription.recordDispense(request.complete());
        // Only a delivery that consumed catalogue stock has a matching
        // pharmacy-cost source for accounting. A zero selling price is still
        // synchronized: its stock/COGS movement remains accounting-relevant.
        if (!linkedStockItems.isEmpty()) {
            // The event is written in the same transaction as the immutable delivery.
            // Its actual HTTP synchronization starts only after this transaction commits.
            pharmacyDispenseAccountingOutboxService.enqueue(dispense.getId(), dispense.getCode());
        }
        prescription.getPassage().getPatient().recordModification(
                auditActor,
                PatientAuditEventType.PRESCRIPTION_DISPENSED,
                (request.complete() ? "Délivrance complète" : "Délivrance partielle")
                        + " enregistrée pour l'ordonnance " + prescription.getCode() + ".",
                dispensedAt);
        return toDispense(dispense, savedItems);
    }

    @Transactional
    public PatientPassageClinicalEntryResponse addClinicalEntry(
            UUID patientId,
            UUID passageId,
            CreatePatientPassageClinicalEntryRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientPassageEntity passage = getPassageForPatientScope(patientId, passageId, accessScope);
        assertCanManagePassageStatus(accessScope, passage);
        if (passage.getStatus() != PatientPassageStatus.OPEN) {
            throw new InvalidPatientPassageStateException(
                    "Le suivi clinique ne peut être modifié que sur un passage en cours.");
        }

        Instant recordedAt = Instant.now();
        PatientPassageClinicalEntryEntity entry = new PatientPassageClinicalEntryEntity(
                UUID.randomUUID(),
                passage.getId(),
                request.entryType(),
                request.clinicalFindings().trim(),
                trimToNull(request.diagnosis()),
                trimToNull(request.carePlan()),
                request.orientation(),
                request.followUpOn(),
                auditActor,
                recordedAt);
        PatientPassageClinicalEntryResponse response = toClinicalEntry(
                patientPassageClinicalEntryRepository.save(entry));
        passage.getPatient().recordModification(
                auditActor,
                PatientAuditEventType.CLINICAL_ENTRY_ADDED,
                "Évolution clinique ajoutée au passage " + passage.getCode() + ".",
                recordedAt);
        return response;
    }

    @Transactional
    public PatientPassagePrescriptionResponse addPrescription(
            UUID patientId,
            UUID passageId,
            CreatePatientPassagePrescriptionRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientPassageEntity passage = getPassageForPatientScope(patientId, passageId, accessScope);
        if (passage.getStatus() != PatientPassageStatus.OPEN) {
            throw new InvalidPatientPassageStateException(
                    "Une ordonnance ne peut être ajoutée que sur un passage en cours.");
        }
        String externalPrescriberName = request.source() == PrescriptionSource.EXTERNAL_PAPER
                ? trimToNull(request.externalPrescriberName())
                : null;
        if (request.source() == PrescriptionSource.EXTERNAL_PAPER && externalPrescriberName == null) {
            throw new InvalidPrescriptionException("Le prescripteur indiqué sur l'ordonnance externe est obligatoire.");
        }

        Instant createdAt = Instant.now();
        PatientPassagePrescriptionEntity prescription = patientPassagePrescriptionRepository.save(
                new PatientPassagePrescriptionEntity(
                        UUID.randomUUID(),
                        nextPrescriptionCode(),
                        passage,
                        request.source(),
                        externalPrescriberName,
                        request.source() == PrescriptionSource.EXTERNAL_PAPER
                                ? trimToNull(request.externalReference())
                                : null,
                        trimToNull(request.notes()),
                        auditActor,
                        createdAt));
        List<PatientPassagePrescriptionItemEntity> items = new ArrayList<>();
        for (int index = 0; index < request.items().size(); index++) {
            var item = request.items().get(index);
            items.add(new PatientPassagePrescriptionItemEntity(
                    UUID.randomUUID(),
                    prescription,
                    item.medicineId(),
                    item.medicineName().trim(),
                    trimToNull(item.dosage()),
                    trimToNull(item.administrationRoute()),
                    trimToNull(item.frequency()),
                    trimToNull(item.duration()),
                    trimToNull(item.quantity()),
                    trimToNull(item.instructions()),
                    index));
        }
        List<PatientPassagePrescriptionItemEntity> savedItems = patientPassagePrescriptionItemRepository.saveAll(items);
        passage.getPatient().recordModification(
                auditActor,
                PatientAuditEventType.PRESCRIPTION_ADDED,
                request.source() == PrescriptionSource.MEDICAL
                        ? "Ordonnance médicale ajoutée au passage " + passage.getCode() + "."
                        : "Ordonnance externe enregistrée au passage " + passage.getCode() + ".",
                createdAt);
        return toPrescription(prescription, savedItems);
    }

    /**
     * Registers an external paper prescription at the pharmacy.
     *
     * <p>A pharmacy sale is still a traceable patient encounter. The passage
     * is deliberately created by the backend, using the pharmacist's assigned
     * hospital (or the hospital explicitly selected by a provincial admin),
     * before the external prescription is attached to it.</p>
     */
    @Transactional
    public PatientPassagePrescriptionResponse createPharmacyExternalPrescription(
            CreatePharmacyExternalPrescriptionRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientPassageResponse passage = createPassage(
                request.patientId(),
                new CreatePatientPassageRequest(
                        request.hospitalId(),
                        PatientPassageType.PHARMACY,
                        "Pharmacie hospitalière",
                        "Vente à la pharmacie – ordonnance externe",
                        null),
                accessScope,
                auditActor);
        return addPrescription(
                request.patientId(),
                passage.id(),
                new CreatePatientPassagePrescriptionRequest(
                        PrescriptionSource.EXTERNAL_PAPER,
                        request.externalPrescriberName(),
                        null,
                        request.notes(),
                        request.items()),
                accessScope,
                auditActor);
    }

    public PatientDuplicateCheckResponse checkDuplicates(
            PatientDuplicateCheckRequest request,
            DataAccessScope accessScope) {
        return new PatientDuplicateCheckResponse(findDuplicateCandidates(
                request.firstName(),
                request.lastName(),
                request.middleName(),
                request.dateOfBirth(),
                request.gender()).stream()
                .filter(patient -> accessScope.canAccessHospital(patient.getRegistrationHospitalCode()))
                .map(this::toSummary)
                .toList());
    }

    @Transactional
    public PatientResponse createPatient(
            CreatePatientRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        HospitalReferenceClient.HospitalReference hospital = hospitalReferenceClient.resolveActiveHospital(
                request.registrationHospitalId());
        String registrationHospitalCode = normalizeCode(hospital.hospitalCode());
        assertAccess(accessScope, registrationHospitalCode);

        assertNoDuplicate(request);

        Instant createdAt = Instant.now();
        PatientEntity patient = new PatientEntity(
                UUID.randomUUID(),
                nextPatientCode(),
                request.firstName().trim(),
                request.lastName().trim(),
                trimToNull(request.middleName()),
                request.dateOfBirth(),
                request.gender(),
                trimToNull(request.phoneNumber()),
                trimToNull(request.email()),
                trimToNull(request.address()),
                nextNationalIdentifier(),
                hospital.hospitalId(),
                registrationHospitalCode,
                createdAt);
        for (int index = 0; index < request.emergencyContacts().size(); index++) {
            var contact = request.emergencyContacts().get(index);
            patient.addEmergencyContact(
                    contact.fullName().trim(),
                    contact.phoneNumber().trim(),
                    contact.relationship(),
                    index);
        }
        patient.recordCreation(auditActor, createdAt);
        return toDetails(patientRepository.save(patient));
    }

    @Transactional
    public PatientPassageResponse createPassage(
            UUID patientId,
            CreatePatientPassageRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());

        HospitalReferenceClient.HospitalReference hospital = resolvePassageHospital(request.hospitalId(), accessScope);
        PatientPassageEntity passage = new PatientPassageEntity(
                UUID.randomUUID(),
                nextPassageCode(),
                patient,
                hospital.hospitalId(),
                normalizeCode(hospital.hospitalCode()),
                request.type(),
                trimToNull(request.serviceName()),
                trimToNull(request.reason()),
                auditActor,
                Instant.now());
        if (request.responsiblePersonnelId() != null) {
            assignResponsiblePersonnel(passage, request.responsiblePersonnelId(), auditActor);
        }
        return toPassage(patientPassageRepository.save(passage));
    }

    @Transactional
    public PatientResponse updatePatient(
            UUID patientId,
            UpdatePatientRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        assertNoDuplicate(request, patient.getId());
        patient.updateProfile(
                request.firstName().trim(),
                request.lastName().trim(),
                trimToNull(request.middleName()),
                request.dateOfBirth(),
                request.gender(),
                trimToNull(request.phoneNumber()),
                trimToNull(request.email()),
                trimToNull(request.address()));
        replaceEmergencyContacts(patient, request.emergencyContacts());
        patient.recordModification(
                auditActor,
                PatientAuditEventType.UPDATED,
                "Informations administratives mises à jour.",
                Instant.now());
        return toDetails(patient);
    }

    @Transactional
    public PatientPassageResponse updatePassageStatus(
            UUID patientId,
            UUID passageId,
            UpdatePatientPassageStatusRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientPassageEntity passage = patientPassageRepository.findById(passageId)
                .orElseThrow(() -> new PatientNotFoundException(passageId.toString()));
        if (!passage.getPatient().getId().equals(patientId)) {
            throw new PatientNotFoundException(passageId.toString());
        }
        assertAccess(accessScope, passage.getPatient().getRegistrationHospitalCode());
        assertCanManagePassageStatus(accessScope, passage);
        if (request.status() == PatientPassageStatus.CLOSED && passage.getResponsiblePersonnelId() == null) {
            throw new InvalidPatientPassageStateException(
                    "Un personnel responsable doit être affecté avant de terminer le passage.");
        }
        if (request.status() == PatientPassageStatus.CLOSED
                && patientPassagePrescriptionRepository.existsByPassage_IdAndStatusIn(
                        passage.getId(), Set.of(PrescriptionStatus.PENDING_DISPENSING, PrescriptionStatus.PARTIALLY_DISPENSED))) {
            throw new InvalidPatientPassageStateException(
                    "Les ordonnances du passage doivent être entièrement délivrées ou annulées avant sa clôture.");
        }
        if (passage.getStatus() != request.status()) {
            passage.changeStatus(request.status(), auditActor, Instant.now());
        }
        return toPassage(passage);
    }

    @Transactional
    public PatientPassageResponse assignPassageResponsiblePersonnel(
            UUID patientId,
            UUID passageId,
            AssignPatientPassageResponsiblePersonnelRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientPassageEntity passage = patientPassageRepository.findById(passageId)
                .orElseThrow(() -> new PatientNotFoundException(passageId.toString()));
        if (!passage.getPatient().getId().equals(patientId)) {
            throw new PatientNotFoundException(passageId.toString());
        }
        assertAccess(accessScope, passage.getPatient().getRegistrationHospitalCode());
        if (passage.getStatus() != PatientPassageStatus.OPEN) {
            throw new InvalidPatientPassageStateException(
                    "Le personnel responsable ne peut être modifié que sur un passage en cours.");
        }
        assignResponsiblePersonnel(passage, request.personnelId(), auditActor);
        return toPassage(passage);
    }

    @Transactional
    public PatientResponse updateStatus(
            UUID patientId,
            UpdatePatientStatusRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        if (patient.isActive() != request.active()) {
            patient.setActive(request.active());
            patient.recordModification(
                    auditActor,
                    PatientAuditEventType.STATUS_CHANGED,
                    request.active() ? "Dossier patient activé." : "Dossier patient désactivé.",
                    Instant.now());
        }
        return toDetails(patient);
    }

    @Transactional
    public PatientDocumentResponse addDocument(
            UUID patientId,
            CreatePatientDocumentRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientEntity patient = getPatientForScope(patientId, accessScope);
        DocumentContent content = validateDocument(request);
        if (request.documentType() == PatientDocumentType.PROFILE_PHOTO) {
            patientDocumentRepository.deleteByPatientIdAndDocumentType(patient.getId(), request.documentType());
        }
        Instant createdAt = Instant.now();
        PatientDocumentEntity document = new PatientDocumentEntity(
                UUID.randomUUID(),
                patient.getId(),
                request.documentType(),
                request.fileName().trim(),
                content.contentType(),
                content.sizeBytes(),
                content.contentBase64(),
                createdAt,
                auditActor.userId(),
                auditActor.username());
        PatientDocumentResponse response = toDocument(patientDocumentRepository.save(document));
        patient.recordModification(
                auditActor,
                PatientAuditEventType.DOCUMENT_ADDED,
                "Pièce ajoutée au dossier : " + documentTypeLabel(request.documentType()) + ".",
                createdAt);
        return response;
    }

    @Transactional
    public void deleteDocument(
            UUID patientId,
            UUID documentId,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        PatientEntity patient = getPatientForScope(patientId, accessScope);
        PatientDocumentEntity document = patientDocumentRepository.findByIdAndPatientId(documentId, patient.getId())
                .orElseThrow(() -> new PatientNotFoundException(documentId.toString()));
        patientDocumentRepository.delete(document);
        patient.recordModification(
                auditActor,
                PatientAuditEventType.DOCUMENT_REMOVED,
                "Pièce retirée du dossier : " + documentTypeLabel(document.getDocumentType()) + ".",
                Instant.now());
    }

    private PatientSummaryResponse toSummary(PatientEntity patient) {
        return new PatientSummaryResponse(
                patient.getId(),
                patient.getCode(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getMiddleName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhoneNumber(),
                patient.getRegistrationHospitalId(),
                patient.getRegistrationHospitalCode(),
                patient.isActive(),
                patient.getCreatedAt());
    }

    private PatientDocumentResponse toDocument(PatientDocumentEntity document) {
        return new PatientDocumentResponse(
                document.getId(),
                document.getDocumentType(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getCreatedAt(),
                document.getCreatedByUsername(),
                document.getContentBase64());
    }

    private PatientResponse toDetails(PatientEntity patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getCode(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getMiddleName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhoneNumber(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getNationalIdentifier(),
                patient.getEmergencyContacts().stream()
                        .map(contact -> new EmergencyContactResponse(
                                contact.getId(),
                                contact.getFullName(),
                                contact.getPhoneNumber(),
                                contact.getRelationship()))
                        .toList(),
                patient.getRegistrationHospitalId(),
                patient.getRegistrationHospitalCode(),
                patient.isActive(),
                patient.getCreatedAt(),
                patient.getCreatedByUsername(),
                patient.getUpdatedAt(),
                patient.getUpdatedByUsername(),
                patient.getAuditEvents().stream()
                        .map(event -> new PatientAuditEventResponse(
                                event.getId(),
                                event.getType(),
                                event.getDescription(),
                                event.getOperatorUsername(),
                                event.getOccurredAt()))
                        .toList());
    }

    private String nextPatientCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = "PAT-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (!patientRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un numéro de dossier patient unique.");
    }

    private String nextNationalIdentifier() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String identifier = "NAT-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
            if (!patientRepository.existsByNationalIdentifierIgnoreCase(identifier)) {
                return identifier;
            }
        }
        throw new IllegalStateException("Impossible de générer un identifiant national patient unique.");
    }

    private String nextPassageCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = "PAS-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (!patientPassageRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un numéro de passage unique.");
    }

    private String nextPrescriptionCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (!patientPassagePrescriptionRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un code d'ordonnance unique.");
    }

    private String nextDispenseCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = "DSP-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (!patientPassagePrescriptionDispenseRepository.existsByCodeIgnoreCase(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de générer un code de délivrance unique.");
    }

    private void assertNoDuplicate(CreatePatientRequest request) {
        if (!findDuplicateCandidates(
                request.firstName(),
                request.lastName(),
                request.middleName(),
                request.dateOfBirth(),
                request.gender()).isEmpty()) {
            throw new DuplicatePatientException("cette identité");
        }
    }

    private void assertNoDuplicate(UpdatePatientRequest request, UUID patientId) {
        boolean anotherPatientExists = findDuplicateCandidates(
                request.firstName(),
                request.lastName(),
                request.middleName(),
                request.dateOfBirth(),
                request.gender()).stream()
                .anyMatch(candidate -> !candidate.getId().equals(patientId));
        if (anotherPatientExists) {
            throw new DuplicatePatientException("cette identité");
        }
    }

    private void replaceEmergencyContacts(
            PatientEntity patient,
            List<com.hopital.patient.application.dto.EmergencyContactRequest> contacts) {
        patient.clearEmergencyContacts();
        // Hibernate may otherwise insert the new contact at order 0 before deleting
        // the existing order 0, violating uk_patient_emergency_contacts_order.
        patientRepository.flush();
        for (int index = 0; index < contacts.size(); index++) {
            var contact = contacts.get(index);
            patient.addEmergencyContact(
                    contact.fullName().trim(),
                    contact.phoneNumber().trim(),
                    contact.relationship(),
                    index);
        }
    }

    private List<PatientEntity> findDuplicateCandidates(
            String firstName,
            String lastName,
            String middleName,
            LocalDate dateOfBirth,
            Gender gender) {
        String normalizedMiddleName = trimToNull(middleName);
        return patientRepository.findByIdentity(
                lastName.trim(),
                firstName.trim(),
                normalizedMiddleName == null ? "" : normalizedMiddleName,
                dateOfBirth,
                gender);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private PatientEntity getPatientForScope(UUID patientId, DataAccessScope accessScope) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        return patient;
    }

    private DocumentContent validateDocument(CreatePatientDocumentRequest request) {
        String contentType = request.contentType().trim().toLowerCase(Locale.ROOT);
        boolean imageOnly = request.documentType() == PatientDocumentType.PROFILE_PHOTO;
        Set<String> acceptedContentTypes = imageOnly ? IMAGE_CONTENT_TYPES : DOCUMENT_CONTENT_TYPES;
        if (!acceptedContentTypes.contains(contentType)) {
            throw new InvalidPatientDocumentException(imageOnly
                    ? "La photo du patient doit être au format JPEG, PNG ou WebP."
                    : "Les documents doivent être au format PDF, Word, JPEG, PNG ou WebP.");
        }

        String contentBase64 = request.contentBase64().replaceAll("\\s", "");
        if (contentBase64.startsWith("data:")) {
            throw new InvalidPatientDocumentException("Le contenu du fichier est invalide.");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(contentBase64);
        } catch (IllegalArgumentException exception) {
            throw new InvalidPatientDocumentException("Le contenu du fichier est invalide.");
        }
        if (decoded.length == 0 || decoded.length > MAX_DOCUMENT_SIZE_BYTES) {
            throw new InvalidPatientDocumentException("Chaque fichier ne peut pas dépasser 2 Mo.");
        }
        return new DocumentContent(contentType, decoded.length, contentBase64);
    }

    private String documentTypeLabel(PatientDocumentType documentType) {
        return switch (documentType) {
            case PROFILE_PHOTO -> "photo du patient";
            case IDENTITY_CARD -> "carte d’identité";
            case PASSPORT -> "passeport";
            case BIRTH_CERTIFICATE -> "acte de naissance";
            case HEALTH_INSURANCE -> "assurance ou prise en charge";
            case REFERRAL_LETTER -> "lettre de référence";
            case OTHER -> "autre pièce";
        };
    }

    private HospitalReferenceClient.HospitalReference resolvePassageHospital(
            UUID requestedHospitalId,
            DataAccessScope accessScope) {
        if (accessScope.provinceWide()) {
            if (requestedHospitalId == null) {
                throw new IllegalArgumentException("Un hôpital est obligatoire pour enregistrer un passage.");
            }
            return hospitalReferenceClient.resolveActiveHospital(requestedHospitalId);
        }
        if (accessScope.hospitalId() == null || accessScope.hospitalCode() == null) {
            throw new DataAccessDeniedException();
        }
        return new HospitalReferenceClient.HospitalReference(
                accessScope.hospitalId(),
                accessScope.hospitalCode(),
                true);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private int stockQuantity(PatientPassagePrescriptionItemEntity item, String dispensedQuantity) {
        try {
            int quantity = Integer.parseInt(dispensedQuantity.trim());
            if (quantity < 1) {
                throw new NumberFormatException("quantity must be positive");
            }
            return quantity;
        } catch (NumberFormatException exception) {
            throw new InvalidPrescriptionException(
                    "La quantité délivrée pour " + item.getMedicineName()
                            + " doit être un nombre entier afin de mettre le stock à jour.");
        }
    }

    private String normalizeSearchFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private void assertAccess(DataAccessScope accessScope, String hospitalCode) {
        if (!accessScope.canAccessHospital(hospitalCode)) {
            throw new DataAccessDeniedException();
        }
    }

    private PatientPassageEntity getPassageForPatientScope(
            UUID patientId,
            UUID passageId,
            DataAccessScope accessScope) {
        PatientPassageEntity passage = patientPassageRepository.findById(passageId)
                .orElseThrow(() -> new PatientNotFoundException(passageId.toString()));
        if (!passage.getPatient().getId().equals(patientId)) {
            throw new PatientNotFoundException(passageId.toString());
        }
        assertAccess(accessScope, passage.getPatient().getRegistrationHospitalCode());
        return passage;
    }

    private PatientPassagePrescriptionEntity getPharmacyPrescriptionForScope(
            UUID prescriptionId,
            DataAccessScope accessScope) {
        PatientPassagePrescriptionEntity prescription = patientPassagePrescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PatientNotFoundException(prescriptionId.toString()));
        assertAccess(accessScope, prescription.getPassage().getHospitalCode());
        return prescription;
    }

    private String requiredHospitalCode(DataAccessScope accessScope) {
        if (accessScope.hospitalCode() == null || accessScope.hospitalCode().isBlank()) {
            throw new DataAccessDeniedException();
        }
        return accessScope.hospitalCode();
    }

    private void assertCanManagePassageStatus(DataAccessScope accessScope, PatientPassageEntity passage) {
        if (!canManagePassageStatus(accessScope, passage)) {
            throw new DataAccessDeniedException(
                    "Seul le personnel responsable ou un administrateur peut modifier le statut de ce passage.");
        }
    }

    private boolean canManagePassageStatus(DataAccessScope accessScope, PatientPassageEntity passage) {
        return accessScope.administrator()
                || (accessScope.personnelId() != null
                && accessScope.personnelId().equals(passage.getResponsiblePersonnelId()));
    }

    private PatientPassageResponse toPassage(PatientPassageEntity passage) {
        return new PatientPassageResponse(
                passage.getId(),
                passage.getCode(),
                passage.getHospitalId(),
                passage.getHospitalCode(),
                passage.getType(),
                passage.getServiceName(),
                passage.getReason(),
                passage.getStatus(),
                passage.getArrivedAt(),
                passage.getClosedAt(),
                passage.getCreatedByUsername(),
                passage.getClosedByUsername(),
                passage.getResponsiblePersonnelId(),
                passage.getResponsiblePersonnelEmployeeNumber(),
                passage.getResponsiblePersonnelName(),
                passage.getResponsiblePersonnelJobTitle(),
                passage.getResponsibleAssignedAt(),
                passage.getResponsibleAssignedByUsername());
    }

    private PatientPassageSummaryResponse toPassageSummary(PatientPassageEntity passage, boolean canManageStatus) {
        PatientEntity patient = passage.getPatient();
        return new PatientPassageSummaryResponse(
                passage.getId(),
                passage.getCode(),
                patient.getId(),
                patient.getCode(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getMiddleName(),
                passage.getHospitalId(),
                passage.getHospitalCode(),
                passage.getType(),
                passage.getServiceName(),
                passage.getReason(),
                passage.getStatus(),
                passage.getArrivedAt(),
                passage.getClosedAt(),
                passage.getCreatedByUsername(),
                passage.getClosedByUsername(),
                passage.getResponsiblePersonnelId(),
                passage.getResponsiblePersonnelEmployeeNumber(),
                passage.getResponsiblePersonnelName(),
                passage.getResponsiblePersonnelJobTitle(),
                passage.getResponsibleAssignedAt(),
                passage.getResponsibleAssignedByUsername(),
                canManageStatus);
    }

    private PatientPassageClinicalEntryResponse toClinicalEntry(PatientPassageClinicalEntryEntity entry) {
        return new PatientPassageClinicalEntryResponse(
                entry.getId(),
                entry.getPassageId(),
                entry.getEntryType(),
                entry.getClinicalFindings(),
                entry.getDiagnosis(),
                entry.getCarePlan(),
                entry.getOrientation(),
                entry.getFollowUpOn(),
                entry.getRecordedAt(),
                entry.getRecordedByUsername());
    }

    private PatientPassagePrescriptionResponse toPrescription(
            PatientPassagePrescriptionEntity prescription,
            List<PatientPassagePrescriptionItemEntity> items) {
        return new PatientPassagePrescriptionResponse(
                prescription.getId(),
                prescription.getCode(),
                prescription.getPassage().getId(),
                prescription.getSource(),
                prescription.getStatus(),
                prescription.getExternalPrescriberName(),
                prescription.getExternalReference(),
                prescription.getNotes(),
                prescription.getCreatedAt(),
                prescription.getCreatedByUsername(),
                items.stream().map(this::toPrescriptionItem).toList());
    }

    private List<PharmacyPrescriptionResponse> toPharmacyPrescriptions(
            List<PatientPassagePrescriptionEntity> prescriptions) {
        if (prescriptions.isEmpty()) {
            return List.of();
        }
        List<UUID> prescriptionIds = prescriptions.stream().map(PatientPassagePrescriptionEntity::getId).toList();
        Map<UUID, List<PatientPassagePrescriptionItemEntity>> itemsByPrescription = new HashMap<>();
        for (PatientPassagePrescriptionItemEntity item : patientPassagePrescriptionItemRepository
                .findAllByPrescription_IdInOrderByDisplayOrderAsc(prescriptionIds)) {
            itemsByPrescription.computeIfAbsent(item.getPrescription().getId(), ignored -> new ArrayList<>()).add(item);
        }

        Map<UUID, List<PatientPassagePrescriptionDispenseEntity>> dispensesByPrescription = new HashMap<>();
        List<PatientPassagePrescriptionDispenseEntity> dispenses = patientPassagePrescriptionDispenseRepository
                .findAllByPrescription_IdInOrderByDispensedAtDesc(prescriptionIds);
        Map<UUID, List<PatientPassagePrescriptionDispenseItemEntity>> dispenseItemsByDispense = new HashMap<>();
        if (!dispenses.isEmpty()) {
            List<UUID> dispenseIds = dispenses.stream().map(PatientPassagePrescriptionDispenseEntity::getId).toList();
            for (PatientPassagePrescriptionDispenseItemEntity item : patientPassagePrescriptionDispenseItemRepository
                    .findAllByDispense_IdInOrderByDispense_IdAsc(dispenseIds)) {
                dispenseItemsByDispense.computeIfAbsent(item.getDispense().getId(), ignored -> new ArrayList<>()).add(item);
            }
            for (PatientPassagePrescriptionDispenseEntity dispense : dispenses) {
                dispensesByPrescription.computeIfAbsent(dispense.getPrescription().getId(), ignored -> new ArrayList<>()).add(dispense);
            }
        }

        return prescriptions.stream().map(prescription -> toPharmacyPrescription(
                prescription,
                itemsByPrescription.getOrDefault(prescription.getId(), List.of()),
                dispensesByPrescription.getOrDefault(prescription.getId(), List.of()),
                dispenseItemsByDispense)).toList();
    }

    private PharmacyPrescriptionResponse toPharmacyPrescription(
            PatientPassagePrescriptionEntity prescription,
            List<PatientPassagePrescriptionItemEntity> items,
            List<PatientPassagePrescriptionDispenseEntity> dispenses,
            Map<UUID, List<PatientPassagePrescriptionDispenseItemEntity>> dispenseItemsByDispense) {
        PatientPassageEntity passage = prescription.getPassage();
        PatientEntity patient = passage.getPatient();
        return new PharmacyPrescriptionResponse(
                prescription.getId(),
                prescription.getCode(),
                patient.getId(),
                patient.getCode(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getMiddleName(),
                passage.getId(),
                passage.getCode(),
                passage.getHospitalId(),
                passage.getHospitalCode(),
                passage.getServiceName(),
                prescription.getSource(),
                prescription.getStatus(),
                prescription.getExternalPrescriberName(),
                prescription.getExternalReference(),
                prescription.getNotes(),
                prescription.getCreatedAt(),
                prescription.getCreatedByUsername(),
                items.stream().map(this::toPrescriptionItem).toList(),
                dispenses.stream().map(dispense -> toDispense(
                        dispense,
                        dispenseItemsByDispense.getOrDefault(dispense.getId(), List.of()))).toList());
    }

    private PrescriptionItemResponse toPrescriptionItem(PatientPassagePrescriptionItemEntity item) {
        return new PrescriptionItemResponse(
                item.getId(),
                item.getMedicineId(),
                item.getMedicineName(),
                item.getDosage(),
                item.getAdministrationRoute(),
                item.getFrequency(),
                item.getDuration(),
                item.getQuantity(),
                item.getInstructions(),
                item.getDisplayOrder());
    }

    private PrescriptionDispenseResponse toDispense(
            PatientPassagePrescriptionDispenseEntity dispense,
            List<PatientPassagePrescriptionDispenseItemEntity> items) {
        return new PrescriptionDispenseResponse(
                dispense.getId(),
                dispense.getCode(),
                dispense.getCompletion(),
                dispense.getTotalAmount(),
                dispense.getPaidAmount(),
                dispense.getCurrency(),
                dispense.getPaymentMethod(),
                dispense.getNotes(),
                dispense.getDispensedAt(),
                dispense.getDispensedByUsername(),
                items.stream().map(item -> new PrescriptionDispenseItemResponse(
                        item.getId(),
                        item.getPrescriptionItem().getId(),
                        item.getPrescriptionItem().getMedicineName(),
                        item.getDispensedQuantity())).toList());
    }

    private void assignResponsiblePersonnel(
            PatientPassageEntity passage,
            UUID personnelId,
            AuditActor auditActor) {
        PersonnelReferenceClient.PersonnelReference personnel = personnelReferenceClient
                .resolveActivePersonnelForHospital(personnelId, passage.getHospitalId());
        String fullName = java.util.stream.Stream.of(personnel.lastName(), personnel.firstName(), personnel.middleName())
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));
        passage.assignResponsiblePersonnel(
                personnel.id(),
                personnel.employeeNumber(),
                fullName,
                personnel.jobTitle(),
                auditActor,
                Instant.now());
    }

    private record DocumentContent(String contentType, int sizeBytes, String contentBase64) {
    }
}
