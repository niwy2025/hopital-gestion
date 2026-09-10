package com.hopital.patient.application.service;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.PatientAuditEventType;
import com.hopital.patient.application.domain.PatientPassageStatus;
import com.hopital.patient.application.domain.PatientPassageType;
import com.hopital.patient.application.domain.PatientTransferStatus;
import com.hopital.patient.application.dto.CreatePatientTransferRequest;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.PatientTransferResponse;
import com.hopital.patient.application.dto.PatientTransferSummaryResponse;
import com.hopital.patient.application.dto.ReceivePatientTransferRequest;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.exception.InvalidPatientPassageStateException;
import com.hopital.patient.application.exception.PatientNotFoundException;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.persistence.entity.PatientPassageEntity;
import com.hopital.patient.infra.persistence.entity.PatientTransferEntity;
import com.hopital.patient.infra.persistence.repository.PatientPassageRepository;
import com.hopital.patient.infra.persistence.repository.PatientTransferRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PatientTransferService {
    private static final Set<String> READ_ROLES = Set.of("HOSPITAL_ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST");
    private final PatientTransferRepository transfers;
    private final PatientPassageRepository passages;
    private final HospitalReferenceClient hospitals;

    public PatientTransferService(PatientTransferRepository transfers, PatientPassageRepository passages,
            HospitalReferenceClient hospitals) {
        this.transfers = transfers;
        this.passages = passages;
        this.hospitals = hospitals;
    }

    public PageResponse<PatientTransferSummaryResponse> search(int page, int size, String query,
            PatientTransferStatus status, String direction, UUID hospitalId, UUID passageId, UUID patientId, DataAccessScope scope) {
        checkReader(scope);
        UUID filterHospitalId = scope.provinceWide() ? hospitalId : scope.hospitalId();
        String filter = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        var result = transfers.findAll((root, criteria, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filterHospitalId != null) {
                Predicate outgoing = builder.equal(root.get("sourcePassage").get("hospitalId"), filterHospitalId);
                Predicate incoming = builder.equal(root.get("destinationHospitalId"), filterHospitalId);
                predicates.add("INCOMING".equals(direction) ? incoming : "OUTGOING".equals(direction) ? outgoing
                        : builder.or(outgoing, incoming));
            }
            if (status != null) predicates.add(builder.equal(root.get("status"), status));
            if (passageId != null) predicates.add(builder.or(
                    builder.equal(root.get("sourcePassage").get("id"), passageId),
                    builder.equal(root.get("destinationPassage").get("id"), passageId)));
            if (patientId != null) predicates.add(builder.equal(root.get("sourcePassage").get("patient").get("id"), patientId));
            if (!filter.isEmpty()) predicates.add(builder.or(
                    builder.like(builder.lower(root.get("code")), "%" + filter + "%"),
                    builder.like(builder.lower(root.get("patientName")), "%" + filter + "%"),
                    builder.like(builder.lower(root.get("sourcePassage").get("patient").get("code")), "%" + filter + "%"),
                    builder.like(builder.lower(root.get("sourcePassage").get("hospitalCode")), "%" + filter + "%"),
                    builder.like(builder.lower(root.get("destinationHospitalCode")), "%" + filter + "%"),
                    builder.like(builder.lower(root.get("sourceHospitalName")), "%" + filter + "%"),
                    builder.like(builder.lower(root.get("destinationHospitalName")), "%" + filter + "%"),
                    builder.like(builder.lower(root.get("externalFacilityName")), "%" + filter + "%")));
            return builder.and(predicates.toArray(Predicate[]::new));
        }, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
                Sort.by("requestedAt").descending().and(Sort.by("id"))));
        return new PageResponse<>(result.map(this::summary).getContent(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public PatientTransferResponse get(UUID id, DataAccessScope scope) {
        PatientTransferEntity transfer = transfers.findById(id).orElseThrow(() -> new PatientNotFoundException(id.toString()));
        checkRead(transfer, scope);
        return response(transfer, scope);
    }

    @Transactional
    public PatientTransferResponse create(UUID passageId, CreatePatientTransferRequest request,
            DataAccessScope scope, AuditActor actor) {
        PatientPassageEntity source = passages.findForUpdate(passageId)
                .orElseThrow(() -> new PatientNotFoundException(passageId.toString()));
        checkSender(source, scope);
        if (source.getStatus() != PatientPassageStatus.OPEN) fail("Le passage doit être en cours pour préparer un transfert.");
        if (transfers.existsBySourcePassage_IdAndStatusNot(passageId, PatientTransferStatus.CANCELLED))
            fail("Un transfert non annulé existe déjà pour ce passage.");
        if (request.reason() == null || request.reason().isBlank()
                || request.clinicalSummary() == null || request.clinicalSummary().isBlank())
            fail("Le motif et la synthèse clinique du transfert sont obligatoires.");
        String destinationCode = null;
        String destinationName = null;
        if (request.destinationHospitalId() != null) {
            if (request.externalFacilityName() != null && !request.externalFacilityName().isBlank())
                fail("Choisissez un hôpital du système ou une structure externe, pas les deux.");
            if (source.getHospitalId().equals(request.destinationHospitalId()))
                fail("L’hôpital destinataire doit être différent de l’hôpital de départ.");
            var destination = hospitals.resolveActiveHospital(request.destinationHospitalId());
            destinationCode = destination.hospitalCode();
            destinationName = destination.hospitalName();
        } else if (request.externalFacilityName() == null || request.externalFacilityName().isBlank()) {
            fail("Le nom de la structure externe est obligatoire.");
        }
        PatientTransferEntity transfer = new PatientTransferEntity(source, request,
                request.destinationHospitalId(), destinationCode, actor);
        transfer.recordHospitalNames(hospitals.resolveActiveHospital(source.getHospitalId()).hospitalName(), destinationName);
        transfers.saveAndFlush(transfer);
        audit(transfer, PatientAuditEventType.TRANSFER_REQUESTED, actor);
        return response(transfer, scope);
    }

    @Transactional
    public PatientTransferResponse dispatch(UUID id, DataAccessScope scope, AuditActor actor) {
        PatientTransferEntity transfer = locked(id);
        PatientPassageEntity source = passages.findForUpdate(transfer.getSourcePassage().getId()).orElseThrow();
        checkSender(source, scope);
        if (transfer.getStatus() == PatientTransferStatus.IN_TRANSIT
                || transfer.getStatus() == PatientTransferStatus.EXTERNAL_COMPLETED) return response(transfer, scope);
        if (transfer.getStatus() != PatientTransferStatus.REQUESTED || source.getStatus() != PatientPassageStatus.OPEN)
            fail("Seul un transfert préparé depuis un passage en cours peut partir.");
        if (transfer.getDestinationHospitalId() != null) hospitals.resolveActiveHospital(transfer.getDestinationHospitalId());
        Instant now = Instant.now();
        source.changeStatus(PatientPassageStatus.TRANSFERRED, actor, now);
        transfer.dispatch(actor, now);
        audit(transfer, PatientAuditEventType.TRANSFER_DISPATCHED, actor);
        return response(transfer, scope);
    }

    @Transactional
    public PatientTransferResponse receive(UUID id, ReceivePatientTransferRequest request,
            DataAccessScope scope, AuditActor actor) {
        PatientTransferEntity transfer = locked(id);
        checkReader(scope);
        if (transfer.getDestinationHospitalId() == null || (!scope.administrator()
                && !transfer.getDestinationHospitalId().equals(scope.hospitalId()))) throw new DataAccessDeniedException();
        if (transfer.getStatus() == PatientTransferStatus.RECEIVED) return response(transfer, scope);
        if (transfer.getStatus() != PatientTransferStatus.IN_TRANSIT) fail("Confirmez le départ avant de réceptionner ce patient.");
        if (request.type() == null || !Set.of(PatientPassageType.CONSULTATION, PatientPassageType.EMERGENCY,
                PatientPassageType.HOSPITALIZATION, PatientPassageType.OTHER).contains(request.type()))
            fail("Choisissez un type de prise en charge à l’arrivée : consultation, urgence, hospitalisation ou autre.");
        var hospital = hospitals.resolveActiveHospital(transfer.getDestinationHospitalId());
        var passage = new PatientPassageEntity(UUID.randomUUID(),
                "PAS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT),
                transfer.getSourcePassage().getPatient(), hospital.hospitalId(), hospital.hospitalCode(),
                request.type(), clean(request.serviceName()), transfer.getReason(), actor, Instant.now());
        passages.saveAndFlush(passage);
        transfer.receive(passage, actor, request.receptionNote());
        audit(transfer, PatientAuditEventType.TRANSFER_RECEIVED, actor);
        return response(transfer, scope);
    }

    @Transactional
    public PatientTransferResponse cancel(UUID id, String reason, DataAccessScope scope, AuditActor actor) {
        PatientTransferEntity transfer = locked(id);
        checkSender(transfer.getSourcePassage(), scope);
        if (transfer.getStatus() == PatientTransferStatus.CANCELLED) return response(transfer, scope);
        if (transfer.getStatus() != PatientTransferStatus.REQUESTED) fail("Un transfert déjà parti ne peut pas être annulé.");
        if (reason == null || reason.isBlank()) fail("Le motif d’annulation est obligatoire.");
        transfer.cancel(actor, reason);
        audit(transfer, PatientAuditEventType.TRANSFER_CANCELLED, actor);
        return response(transfer, scope);
    }

    private PatientTransferEntity locked(UUID id) {
        return transfers.findForUpdate(id).orElseThrow(() -> new PatientNotFoundException(id.toString()));
    }

    private void checkReader(DataAccessScope scope) {
        if (!scope.administrator() && (scope.hospitalId() == null
                || scope.roleCodes().stream().noneMatch(READ_ROLES::contains))) throw new DataAccessDeniedException();
    }

    private void checkRead(PatientTransferEntity transfer, DataAccessScope scope) {
        checkReader(scope);
        if (!scope.provinceWide() && !transfer.getSourcePassage().getHospitalId().equals(scope.hospitalId())
                && !java.util.Objects.equals(transfer.getDestinationHospitalId(), scope.hospitalId()))
            throw new DataAccessDeniedException();
    }

    private boolean canSend(PatientPassageEntity source, DataAccessScope scope) {
        return scope.administrator() || (source.getHospitalId().equals(scope.hospitalId())
                && (scope.roleCodes().contains("HOSPITAL_ADMIN")
                    || (scope.roleCodes().contains("DOCTOR") && scope.personnelId() != null
                        && scope.personnelId().equals(source.getResponsiblePersonnelId()))));
    }

    private void checkSender(PatientPassageEntity source, DataAccessScope scope) {
        if (!canSend(source, scope)) throw new DataAccessDeniedException(
                "Le transfert est réservé au médecin responsable ou à l’administrateur de l’hôpital.");
    }

    private void audit(PatientTransferEntity transfer, PatientAuditEventType type, AuditActor actor) {
        transfer.getSourcePassage().getPatient().recordModification(actor, type,
                "Transfert " + transfer.getCode() + " : " + transfer.getStatus().name(), Instant.now());
    }

    private PatientTransferSummaryResponse summary(PatientTransferEntity transfer) {
        var source = transfer.getSourcePassage();
        return new PatientTransferSummaryResponse(transfer.getId(), transfer.getCode(), source.getPatient().getCode(),
                transfer.getPatientName(), source.getHospitalCode(), transfer.getSourceHospitalName(),
                transfer.getDestinationHospitalCode(), transfer.getDestinationHospitalName(), transfer.getExternalFacilityName(),
                transfer.isUrgent(), transfer.getStatus(), transfer.getRequestedAt());
    }

    private PatientTransferResponse response(PatientTransferEntity transfer, DataAccessScope scope) {
        var source = transfer.getSourcePassage();
        var destination = transfer.getDestinationPassage();
        boolean prepared = transfer.getStatus() == PatientTransferStatus.REQUESTED;
        return new PatientTransferResponse(
                transfer.getId(), transfer.getCode(), source.getPatient().getId(), source.getPatient().getCode(),
                transfer.getPatientName(), transfer.getPatientDateOfBirth(), transfer.getPatientGender(),
                source.getId(), source.getCode(), source.getHospitalId(), source.getHospitalCode(),
                transfer.getDestinationHospitalId(), transfer.getDestinationHospitalCode(),
                destination == null ? null : destination.getId(), destination == null ? null : destination.getCode(),
                transfer.getSourceHospitalName(), transfer.getDestinationHospitalName(),
                transfer.getExternalFacilityName(), transfer.getExternalFacilityAddress(), transfer.getDestinationContact(),
                transfer.getReason(), transfer.getClinicalSummary(), transfer.getTreatmentAndInstructions(), transfer.getTransportDetails(),
                transfer.isUrgent(), transfer.getStatus(), transfer.getRequestedAt(), transfer.getRequestedByUsername(),
                transfer.getDispatchedAt(), transfer.getDispatchedByUsername(), transfer.getResolvedAt(),
                transfer.getResolvedByUsername(), transfer.getResolutionNote(),
                prepared && source.getStatus() == PatientPassageStatus.OPEN && canSend(source, scope),
                prepared && canSend(source, scope),
                transfer.getStatus() == PatientTransferStatus.IN_TRANSIT && transfer.getDestinationHospitalId() != null
                        && (scope.administrator() || transfer.getDestinationHospitalId().equals(scope.hospitalId())));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void fail(String message) {
        throw new InvalidPatientPassageStateException(message);
    }
}
