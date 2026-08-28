package com.hopital.patient.application.service;

import com.hopital.patient.application.dto.CreatePatientRequest;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.exception.DataAccessDeniedException;
import com.hopital.patient.application.dto.PatientResponse;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.UpdatePatientStatusRequest;
import com.hopital.patient.application.exception.DuplicatePatientException;
import com.hopital.patient.application.exception.PatientNotFoundException;
import com.hopital.patient.infra.persistence.entity.PatientEntity;
import com.hopital.patient.infra.persistence.repository.PatientRepository;
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
public class PatientApplicationService {

    private final PatientRepository patientRepository;

    public PatientApplicationService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponse> listPatients(DataAccessScope accessScope) {
        List<PatientEntity> patients = accessScope.provinceWide()
                ? patientRepository.findAllByOrderByLastNameAscFirstNameAsc()
                : patientRepository.findAllByRegistrationHospitalCodeIgnoreCaseOrderByLastNameAscFirstNameAsc(accessScope.hospitalCode());
        return patients.stream().map(this::toResponse).toList();
    }

    public PageResponse<PatientResponse> searchPatients(int page, int size, String query, DataAccessScope accessScope) {
        var patients = patientRepository.search(
                normalizeSearchFilter(query),
                accessScope.provinceWide() ? "" : accessScope.hospitalCode(),
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("lastName").ascending().and(Sort.by("firstName").ascending())));
        return new PageResponse<>(
                patients.getContent().stream().map(this::toResponse).toList(),
                patients.getNumber(),
                patients.getSize(),
                patients.getTotalElements(),
                patients.getTotalPages());
    }

    @Transactional
    public PatientResponse createPatient(CreatePatientRequest request, DataAccessScope accessScope) {
        String code = normalizeCode(request.code());
        String registrationHospitalCode = normalizeCode(request.registrationHospitalCode());
        assertAccess(accessScope, registrationHospitalCode);
        if (patientRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicatePatientException(code);
        }

        PatientEntity patient = new PatientEntity(
                UUID.randomUUID(),
                code,
                request.firstName().trim(),
                request.lastName().trim(),
                request.dateOfBirth(),
                request.gender(),
                trimToNull(request.phoneNumber()),
                trimToNull(request.address()),
                registrationHospitalCode,
                Instant.now());
        return toResponse(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse updateStatus(String patientCode, UpdatePatientStatusRequest request, DataAccessScope accessScope) {
        PatientEntity patient = patientRepository.findByCodeIgnoreCase(normalizeCode(patientCode))
                .orElseThrow(() -> new PatientNotFoundException(patientCode));
        assertAccess(accessScope, patient.getRegistrationHospitalCode());
        patient.setActive(request.active());
        return toResponse(patient);
    }

    private PatientResponse toResponse(PatientEntity patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getCode(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhoneNumber(),
                patient.getAddress(),
                patient.getRegistrationHospitalCode(),
                patient.isActive(),
                patient.getCreatedAt());
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

    private String normalizeSearchFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private void assertAccess(DataAccessScope accessScope, String hospitalCode) {
        if (!accessScope.canAccessHospital(hospitalCode)) {
            throw new DataAccessDeniedException();
        }
    }
}
