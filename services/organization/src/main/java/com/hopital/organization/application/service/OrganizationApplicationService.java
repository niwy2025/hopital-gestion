package com.hopital.organization.application.service;

import com.hopital.organization.application.dto.CreateHealthZoneRequest;
import com.hopital.organization.application.dto.CreateHospitalRequest;
import com.hopital.organization.application.dto.CreateLaboratoryStructureRequest;
import com.hopital.organization.application.dto.CreateProvinceRequest;
import com.hopital.organization.application.dto.CreateReferenceLaboratoryRequest;
import com.hopital.organization.application.dto.HealthZoneResponse;
import com.hopital.organization.application.dto.HospitalResponse;
import com.hopital.organization.application.dto.LaboratoryStructureResponse;
import com.hopital.organization.application.dto.ProvinceResponse;
import com.hopital.organization.application.dto.ReferenceLaboratoryResponse;
import com.hopital.organization.application.dto.UpdateOrganizationStatusRequest;
import com.hopital.organization.application.exception.DuplicateOrganizationException;
import com.hopital.organization.application.exception.OrganizationNotFoundException;
import com.hopital.organization.infra.persistence.entity.HealthZoneEntity;
import com.hopital.organization.infra.persistence.entity.HospitalEntity;
import com.hopital.organization.infra.persistence.entity.LaboratoryStructureEntity;
import com.hopital.organization.infra.persistence.entity.ProvinceEntity;
import com.hopital.organization.infra.persistence.entity.ReferenceLaboratoryEntity;
import com.hopital.organization.infra.persistence.repository.HealthZoneRepository;
import com.hopital.organization.infra.persistence.repository.HospitalRepository;
import com.hopital.organization.infra.persistence.repository.LaboratoryStructureRepository;
import com.hopital.organization.infra.persistence.repository.ProvinceRepository;
import com.hopital.organization.infra.persistence.repository.ReferenceLaboratoryRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrganizationApplicationService {

    private final ProvinceRepository provinceRepository;
    private final HealthZoneRepository healthZoneRepository;
    private final HospitalRepository hospitalRepository;
    private final ReferenceLaboratoryRepository referenceLaboratoryRepository;
    private final LaboratoryStructureRepository laboratoryStructureRepository;

    public OrganizationApplicationService(
            ProvinceRepository provinceRepository,
            HealthZoneRepository healthZoneRepository,
            HospitalRepository hospitalRepository,
            ReferenceLaboratoryRepository referenceLaboratoryRepository,
            LaboratoryStructureRepository laboratoryStructureRepository) {
        this.provinceRepository = provinceRepository;
        this.healthZoneRepository = healthZoneRepository;
        this.hospitalRepository = hospitalRepository;
        this.referenceLaboratoryRepository = referenceLaboratoryRepository;
        this.laboratoryStructureRepository = laboratoryStructureRepository;
    }

    public List<ProvinceResponse> listProvinces() {
        return provinceRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public List<HealthZoneResponse> listHealthZones() {
        return healthZoneRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public List<HospitalResponse> listHospitals() {
        return hospitalRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public List<ReferenceLaboratoryResponse> listReferenceLaboratories() {
        return referenceLaboratoryRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public List<LaboratoryStructureResponse> listLaboratoryStructures() {
        return laboratoryStructureRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProvinceResponse createProvince(CreateProvinceRequest request) {
        String code = normalizeCode(request.code());
        if (provinceRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateOrganizationException("La province", code);
        }
        return toResponse(provinceRepository.save(new ProvinceEntity(code, request.name().trim())));
    }

    @Transactional
    public ProvinceResponse updateProvinceStatus(String provinceCode, UpdateOrganizationStatusRequest request) {
        ProvinceEntity province = provinceRepository.findByCodeIgnoreCase(normalizeCode(provinceCode))
                .orElseThrow(() -> new OrganizationNotFoundException("La province", provinceCode));
        province.setActive(request.active());
        return toResponse(province);
    }

    @Transactional
    public HealthZoneResponse updateHealthZoneStatus(String healthZoneCode, UpdateOrganizationStatusRequest request) {
        HealthZoneEntity healthZone = healthZoneRepository.findByCodeIgnoreCase(normalizeCode(healthZoneCode))
                .orElseThrow(() -> new OrganizationNotFoundException("La zone de santé", healthZoneCode));
        healthZone.setActive(request.active());
        return toResponse(healthZone);
    }

    @Transactional
    public HospitalResponse updateHospitalStatus(String hospitalCode, UpdateOrganizationStatusRequest request) {
        HospitalEntity hospital = hospitalRepository.findByCodeIgnoreCase(normalizeCode(hospitalCode))
                .orElseThrow(() -> new OrganizationNotFoundException("L'établissement", hospitalCode));
        hospital.setActive(request.active());
        return toResponse(hospital);
    }

    @Transactional
    public ReferenceLaboratoryResponse updateReferenceLaboratoryStatus(
            String referenceLaboratoryCode, UpdateOrganizationStatusRequest request) {
        ReferenceLaboratoryEntity referenceLaboratory = referenceLaboratoryRepository
                .findByCodeIgnoreCase(normalizeCode(referenceLaboratoryCode))
                .orElseThrow(() -> new OrganizationNotFoundException("Le laboratoire de référence", referenceLaboratoryCode));
        referenceLaboratory.setActive(request.active());
        return toResponse(referenceLaboratory);
    }

    @Transactional
    public LaboratoryStructureResponse updateLaboratoryStructureStatus(
            String laboratoryStructureCode, UpdateOrganizationStatusRequest request) {
        LaboratoryStructureEntity laboratoryStructure = laboratoryStructureRepository
                .findByCodeIgnoreCase(normalizeCode(laboratoryStructureCode))
                .orElseThrow(() -> new OrganizationNotFoundException(
                        "La structure de laboratoire", laboratoryStructureCode));
        laboratoryStructure.setActive(request.active());
        return toResponse(laboratoryStructure);
    }

    @Transactional
    public HealthZoneResponse createHealthZone(CreateHealthZoneRequest request) {
        String code = normalizeCode(request.code());
        if (healthZoneRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateOrganizationException("La zone de santé", code);
        }
        ProvinceEntity province = provinceRepository.findByCodeIgnoreCase(normalizeCode(request.provinceCode()))
                .orElseThrow(() -> new OrganizationNotFoundException("La province", request.provinceCode()));
        return toResponse(healthZoneRepository.save(new HealthZoneEntity(code, request.name().trim(), province)));
    }

    @Transactional
    public HospitalResponse createHospital(CreateHospitalRequest request) {
        String code = normalizeCode(request.code());
        if (hospitalRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateOrganizationException("L'établissement", code);
        }
        HealthZoneEntity healthZone = healthZoneRepository.findByCodeIgnoreCase(normalizeCode(request.healthZoneCode()))
                .orElseThrow(() -> new OrganizationNotFoundException("La zone de santé", request.healthZoneCode()));
        HospitalEntity hospital = new HospitalEntity(
                UUID.randomUUID(),
                code,
                request.name().trim(),
                request.type(),
                healthZone,
                trimToNull(request.address()),
                trimToNull(request.phoneNumber()));
        return toResponse(hospitalRepository.save(hospital));
    }

    @Transactional
    public ReferenceLaboratoryResponse createReferenceLaboratory(CreateReferenceLaboratoryRequest request) {
        String code = normalizeCode(request.code());
        if (referenceLaboratoryRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateOrganizationException("Le laboratoire de référence", code);
        }
        ProvinceEntity province = provinceRepository.findByCodeIgnoreCase(normalizeCode(request.provinceCode()))
                .orElseThrow(() -> new OrganizationNotFoundException("La province", request.provinceCode()));
        ReferenceLaboratoryEntity referenceLaboratory = new ReferenceLaboratoryEntity(
                UUID.randomUUID(),
                code,
                request.name().trim(),
                province,
                trimToNull(request.address()),
                trimToNull(request.phoneNumber()));
        return toResponse(referenceLaboratoryRepository.save(referenceLaboratory));
    }

    @Transactional
    public LaboratoryStructureResponse createLaboratoryStructure(CreateLaboratoryStructureRequest request) {
        String code = normalizeCode(request.code());
        if (laboratoryStructureRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateOrganizationException("La structure de laboratoire", code);
        }
        ReferenceLaboratoryEntity referenceLaboratory = referenceLaboratoryRepository
                .findByCodeIgnoreCase(normalizeCode(request.referenceLaboratoryCode()))
                .orElseThrow(() -> new OrganizationNotFoundException(
                        "Le laboratoire de référence", request.referenceLaboratoryCode()));
        LaboratoryStructureEntity laboratoryStructure = new LaboratoryStructureEntity(
                UUID.randomUUID(), code, request.name().trim(), request.type(), referenceLaboratory);
        return toResponse(laboratoryStructureRepository.save(laboratoryStructure));
    }

    private ProvinceResponse toResponse(ProvinceEntity province) {
        return new ProvinceResponse(province.getCode(), province.getName(), province.isActive());
    }

    private HealthZoneResponse toResponse(HealthZoneEntity healthZone) {
        return new HealthZoneResponse(
                healthZone.getCode(), healthZone.getName(), healthZone.getProvince().getCode(), healthZone.isActive());
    }

    private HospitalResponse toResponse(HospitalEntity hospital) {
        HealthZoneEntity healthZone = hospital.getHealthZone();
        return new HospitalResponse(
                hospital.getId(),
                hospital.getCode(),
                hospital.getName(),
                hospital.getType(),
                healthZone.getProvince().getCode(),
                healthZone.getCode(),
                hospital.getAddress(),
                hospital.getPhoneNumber(),
                hospital.isActive());
    }

    private ReferenceLaboratoryResponse toResponse(ReferenceLaboratoryEntity referenceLaboratory) {
        return new ReferenceLaboratoryResponse(
                referenceLaboratory.getId(),
                referenceLaboratory.getCode(),
                referenceLaboratory.getName(),
                referenceLaboratory.getProvince().getCode(),
                referenceLaboratory.getAddress(),
                referenceLaboratory.getPhoneNumber(),
                referenceLaboratory.isActive());
    }

    private LaboratoryStructureResponse toResponse(LaboratoryStructureEntity laboratoryStructure) {
        return new LaboratoryStructureResponse(
                laboratoryStructure.getId(),
                laboratoryStructure.getCode(),
                laboratoryStructure.getName(),
                laboratoryStructure.getType(),
                laboratoryStructure.getReferenceLaboratory().getCode(),
                laboratoryStructure.isActive());
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
