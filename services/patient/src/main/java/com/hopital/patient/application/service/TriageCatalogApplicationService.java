package com.hopital.patient.application.service;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.TriageMeasurementValueKind;
import com.hopital.patient.application.dto.CreateHospitalTriageAlertSignRequest;
import com.hopital.patient.application.dto.CreateHospitalTriageMeasurementRequest;
import com.hopital.patient.application.dto.HospitalTriageAlertSignResponse;
import com.hopital.patient.application.dto.HospitalTriageMeasurementResponse;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.UpdateHospitalTriageAlertSignRequest;
import com.hopital.patient.application.dto.UpdateHospitalTriageMeasurementRequest;
import com.hopital.patient.application.dto.UpdateTriageCatalogStatusRequest;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.exception.InvalidTriageCatalogException;
import com.hopital.patient.application.exception.PatientNotFoundException;
import com.hopital.patient.infra.integration.organization.HospitalReferenceClient;
import com.hopital.patient.infra.persistence.entity.HospitalTriageAlertSignEntity;
import com.hopital.patient.infra.persistence.entity.HospitalTriageMeasurementEntity;
import com.hopital.patient.infra.persistence.repository.HospitalTriageAlertSignRepository;
import com.hopital.patient.infra.persistence.repository.HospitalTriageMeasurementRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TriageCatalogApplicationService {

    private final HospitalTriageMeasurementRepository measurementRepository;
    private final HospitalTriageAlertSignRepository alertSignRepository;
    private final HospitalReferenceClient hospitalReferenceClient;

    public TriageCatalogApplicationService(
            HospitalTriageMeasurementRepository measurementRepository,
            HospitalTriageAlertSignRepository alertSignRepository,
            HospitalReferenceClient hospitalReferenceClient) {
        this.measurementRepository = measurementRepository;
        this.alertSignRepository = alertSignRepository;
        this.hospitalReferenceClient = hospitalReferenceClient;
    }

    public PageResponse<HospitalTriageMeasurementResponse> searchMeasurements(
            UUID hospitalId,
            int page,
            int size,
            String query,
            Boolean active,
            DataAccessScope accessScope) {
        UUID scopedHospitalId = resolveReadableHospital(hospitalId, accessScope).hospitalId();
        var result = measurementRepository.search(
                scopedHospitalId,
                normalizeQuery(query),
                active,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("displayOrder").ascending().and(Sort.by("label").ascending())));
        return new PageResponse<>(
                result.getContent().stream().map(this::toMeasurement).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    public List<HospitalTriageMeasurementResponse> listActiveMeasurements(
            UUID hospitalId,
            DataAccessScope accessScope) {
        UUID scopedHospitalId = resolveReadableHospital(hospitalId, accessScope).hospitalId();
        return measurementRepository
                .findAllByHospitalIdAndActiveTrueOrderByDisplayOrderAscLabelAsc(scopedHospitalId)
                .stream()
                .map(this::toMeasurement)
                .toList();
    }

    @Transactional
    public HospitalTriageMeasurementResponse createMeasurement(
            UUID hospitalId,
            CreateHospitalTriageMeasurementRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        UUID scopedHospitalId = resolveManagedHospital(hospitalId, accessScope).hospitalId();
        String code = normalizeCode(request.code());
        if (measurementRepository.existsByHospitalIdAndCodeIgnoreCase(scopedHospitalId, code)) {
            throw new InvalidTriageCatalogException("Une mesure avec ce code existe déjà pour cet hôpital.");
        }
        validateMeasurementBounds(request.valueKind(), request.minValue(), request.maxValue());
        Instant now = Instant.now();
        HospitalTriageMeasurementEntity saved = measurementRepository.save(new HospitalTriageMeasurementEntity(
                UUID.randomUUID(),
                scopedHospitalId,
                code,
                request.label().trim(),
                trimToNull(request.unit()),
                request.valueKind(),
                request.minValue(),
                request.maxValue(),
                request.displayOrder() == null ? 0 : request.displayOrder(),
                auditActor,
                now));
        return toMeasurement(saved);
    }

    @Transactional
    public HospitalTriageMeasurementResponse updateMeasurement(
            UUID hospitalId,
            UUID measurementId,
            UpdateHospitalTriageMeasurementRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        UUID scopedHospitalId = resolveManagedHospital(hospitalId, accessScope).hospitalId();
        HospitalTriageMeasurementEntity measurement = measurementRepository
                .findByIdAndHospitalId(measurementId, scopedHospitalId)
                .orElseThrow(() -> new PatientNotFoundException(measurementId.toString()));
        validateMeasurementBounds(request.valueKind(), request.minValue(), request.maxValue());
        measurement.update(
                request.label().trim(),
                trimToNull(request.unit()),
                request.valueKind(),
                request.minValue(),
                request.maxValue(),
                request.displayOrder() == null ? measurement.getDisplayOrder() : request.displayOrder(),
                auditActor,
                Instant.now());
        return toMeasurement(measurement);
    }

    @Transactional
    public HospitalTriageMeasurementResponse updateMeasurementStatus(
            UUID hospitalId,
            UUID measurementId,
            UpdateTriageCatalogStatusRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        UUID scopedHospitalId = resolveManagedHospital(hospitalId, accessScope).hospitalId();
        HospitalTriageMeasurementEntity measurement = measurementRepository
                .findByIdAndHospitalId(measurementId, scopedHospitalId)
                .orElseThrow(() -> new PatientNotFoundException(measurementId.toString()));
        measurement.changeStatus(Boolean.TRUE.equals(request.active()), auditActor, Instant.now());
        return toMeasurement(measurement);
    }

    public PageResponse<HospitalTriageAlertSignResponse> searchAlertSigns(
            UUID hospitalId,
            int page,
            int size,
            String query,
            Boolean active,
            DataAccessScope accessScope) {
        UUID scopedHospitalId = resolveReadableHospital(hospitalId, accessScope).hospitalId();
        var result = alertSignRepository.search(
                scopedHospitalId,
                normalizeQuery(query),
                active,
                PageRequest.of(
                        Math.max(page, 0),
                        Math.min(Math.max(size, 1), 100),
                        Sort.by("displayOrder").ascending().and(Sort.by("label").ascending())));
        return new PageResponse<>(
                result.getContent().stream().map(this::toAlertSign).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    public List<HospitalTriageAlertSignResponse> listActiveAlertSigns(
            UUID hospitalId,
            DataAccessScope accessScope) {
        UUID scopedHospitalId = resolveReadableHospital(hospitalId, accessScope).hospitalId();
        return alertSignRepository
                .findAllByHospitalIdAndActiveTrueOrderByDisplayOrderAscLabelAsc(scopedHospitalId)
                .stream()
                .map(this::toAlertSign)
                .toList();
    }

    @Transactional
    public HospitalTriageAlertSignResponse createAlertSign(
            UUID hospitalId,
            CreateHospitalTriageAlertSignRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        UUID scopedHospitalId = resolveManagedHospital(hospitalId, accessScope).hospitalId();
        String code = normalizeCode(request.code());
        if (alertSignRepository.existsByHospitalIdAndCodeIgnoreCase(scopedHospitalId, code)) {
            throw new InvalidTriageCatalogException("Un signe d’alerte avec ce code existe déjà pour cet hôpital.");
        }
        Instant now = Instant.now();
        HospitalTriageAlertSignEntity saved = alertSignRepository.save(new HospitalTriageAlertSignEntity(
                UUID.randomUUID(),
                scopedHospitalId,
                code,
                request.label().trim(),
                request.displayOrder() == null ? 0 : request.displayOrder(),
                auditActor,
                now));
        return toAlertSign(saved);
    }

    @Transactional
    public HospitalTriageAlertSignResponse updateAlertSign(
            UUID hospitalId,
            UUID alertSignId,
            UpdateHospitalTriageAlertSignRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        UUID scopedHospitalId = resolveManagedHospital(hospitalId, accessScope).hospitalId();
        HospitalTriageAlertSignEntity alertSign = alertSignRepository
                .findByIdAndHospitalId(alertSignId, scopedHospitalId)
                .orElseThrow(() -> new PatientNotFoundException(alertSignId.toString()));
        alertSign.update(
                request.label().trim(),
                request.displayOrder() == null ? alertSign.getDisplayOrder() : request.displayOrder(),
                auditActor,
                Instant.now());
        return toAlertSign(alertSign);
    }

    @Transactional
    public HospitalTriageAlertSignResponse updateAlertSignStatus(
            UUID hospitalId,
            UUID alertSignId,
            UpdateTriageCatalogStatusRequest request,
            DataAccessScope accessScope,
            AuditActor auditActor) {
        UUID scopedHospitalId = resolveManagedHospital(hospitalId, accessScope).hospitalId();
        HospitalTriageAlertSignEntity alertSign = alertSignRepository
                .findByIdAndHospitalId(alertSignId, scopedHospitalId)
                .orElseThrow(() -> new PatientNotFoundException(alertSignId.toString()));
        alertSign.changeStatus(Boolean.TRUE.equals(request.active()), auditActor, Instant.now());
        return toAlertSign(alertSign);
    }

    private HospitalReferenceClient.HospitalReference resolveReadableHospital(
            UUID hospitalId,
            DataAccessScope accessScope) {
        if (!accessScope.canReadTriage()) {
            throw new DataAccessDeniedException();
        }
        HospitalReferenceClient.HospitalReference hospital = hospitalReferenceClient.resolveActiveHospital(hospitalId);
        if (!accessScope.administrator() && !hospital.hospitalId().equals(accessScope.hospitalId())) {
            throw new DataAccessDeniedException();
        }
        return hospital;
    }

    private HospitalReferenceClient.HospitalReference resolveManagedHospital(
            UUID hospitalId,
            DataAccessScope accessScope) {
        if (!accessScope.canManageTriageCatalog()) {
            throw new DataAccessDeniedException();
        }
        return resolveReadableHospital(hospitalId, accessScope);
    }

    private void validateMeasurementBounds(
            TriageMeasurementValueKind valueKind,
            BigDecimal minValue,
            BigDecimal maxValue) {
        if (valueKind == TriageMeasurementValueKind.TEXT && (minValue != null || maxValue != null)) {
            throw new InvalidTriageCatalogException("Les bornes min/max ne s’appliquent qu’aux mesures numériques.");
        }
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new InvalidTriageCatalogException("La borne minimale ne peut pas dépasser la borne maximale.");
        }
    }

    private HospitalTriageMeasurementResponse toMeasurement(HospitalTriageMeasurementEntity measurement) {
        return new HospitalTriageMeasurementResponse(
                measurement.getId(),
                measurement.getHospitalId(),
                measurement.getCode(),
                measurement.getLabel(),
                measurement.getUnit(),
                measurement.getValueKind(),
                measurement.getMinValue(),
                measurement.getMaxValue(),
                measurement.getDisplayOrder(),
                measurement.isActive(),
                measurement.getCreatedAt(),
                measurement.getCreatedByUsername(),
                measurement.getUpdatedAt(),
                measurement.getUpdatedByUsername());
    }

    private HospitalTriageAlertSignResponse toAlertSign(HospitalTriageAlertSignEntity alertSign) {
        return new HospitalTriageAlertSignResponse(
                alertSign.getId(),
                alertSign.getHospitalId(),
                alertSign.getCode(),
                alertSign.getLabel(),
                alertSign.getDisplayOrder(),
                alertSign.isActive(),
                alertSign.getCreatedAt(),
                alertSign.getCreatedByUsername(),
                alertSign.getUpdatedAt(),
                alertSign.getUpdatedByUsername());
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
