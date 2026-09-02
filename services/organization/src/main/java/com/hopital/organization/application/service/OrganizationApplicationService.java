package com.hopital.organization.application.service;

import com.hopital.organization.application.dto.CreateHealthZoneRequest;
import com.hopital.organization.application.dto.CreateHealthAreaRequest;
import com.hopital.organization.application.domain.HospitalType;
import com.hopital.organization.application.dto.CreateHospitalRequest;
import com.hopital.organization.application.dto.CreateHospitalLaboratoryRequest;
import com.hopital.organization.application.dto.CreateLaboratoryStructureRequest;
import com.hopital.organization.application.dto.CreateProvinceRequest;
import com.hopital.organization.application.dto.CreateReferenceLaboratoryRequest;
import com.hopital.organization.application.dto.HealthZoneResponse;
import com.hopital.organization.application.dto.HealthAreaResponse;
import com.hopital.organization.application.dto.HospitalResponse;
import com.hopital.organization.application.dto.HospitalAccessReferenceResponse;
import com.hopital.organization.application.dto.HospitalLaboratoryAccessReference;
import com.hopital.organization.application.dto.HospitalLaboratoryResponse;
import com.hopital.organization.application.dto.LaboratoryStructureResponse;
import com.hopital.organization.application.dto.PageResponse;
import com.hopital.organization.application.dto.ProvinceResponse;
import com.hopital.organization.application.dto.ReferenceLaboratoryResponse;
import com.hopital.organization.application.dto.ReferenceLaboratoryAccessReference;
import com.hopital.organization.application.dto.UpdateOrganizationStatusRequest;
import com.hopital.organization.application.exception.DuplicateOrganizationException;
import com.hopital.organization.application.exception.OrganizationNotFoundException;
import com.hopital.organization.infra.persistence.entity.HealthZoneEntity;
import com.hopital.organization.infra.persistence.entity.HealthAreaEntity;
import com.hopital.organization.infra.persistence.entity.HospitalEntity;
import com.hopital.organization.infra.persistence.entity.HospitalLaboratoryEntity;
import com.hopital.organization.infra.persistence.entity.LaboratoryStructureEntity;
import com.hopital.organization.infra.persistence.entity.ProvinceEntity;
import com.hopital.organization.infra.persistence.entity.ReferenceLaboratoryEntity;
import com.hopital.organization.infra.persistence.repository.HealthZoneRepository;
import com.hopital.organization.infra.persistence.repository.HealthAreaRepository;
import com.hopital.organization.infra.persistence.repository.HospitalRepository;
import com.hopital.organization.infra.persistence.repository.HospitalLaboratoryRepository;
import com.hopital.organization.infra.persistence.repository.LaboratoryStructureRepository;
import com.hopital.organization.infra.persistence.repository.ProvinceRepository;
import com.hopital.organization.infra.persistence.repository.ReferenceLaboratoryRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrganizationApplicationService {

    private final ProvinceRepository provinceRepository;
    private final HealthZoneRepository healthZoneRepository;
    private final HealthAreaRepository healthAreaRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalLaboratoryRepository hospitalLaboratoryRepository;
    private final ReferenceLaboratoryRepository referenceLaboratoryRepository;
    private final LaboratoryStructureRepository laboratoryStructureRepository;

    public OrganizationApplicationService(
            ProvinceRepository provinceRepository,
            HealthZoneRepository healthZoneRepository,
            HealthAreaRepository healthAreaRepository,
            HospitalRepository hospitalRepository,
            HospitalLaboratoryRepository hospitalLaboratoryRepository,
            ReferenceLaboratoryRepository referenceLaboratoryRepository,
            LaboratoryStructureRepository laboratoryStructureRepository) {
        this.provinceRepository = provinceRepository;
        this.healthZoneRepository = healthZoneRepository;
        this.healthAreaRepository = healthAreaRepository;
        this.hospitalRepository = hospitalRepository;
        this.hospitalLaboratoryRepository = hospitalLaboratoryRepository;
        this.referenceLaboratoryRepository = referenceLaboratoryRepository;
        this.laboratoryStructureRepository = laboratoryStructureRepository;
    }

    public List<ProvinceResponse> listProvinces() {
        return provinceRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public PageResponse<ProvinceResponse> searchProvinces(int page, int size, String query) {
        return toPageResponse(provinceRepository.search(normalizeFilter(query), pageRequest(page, size)), this::toResponse);
    }

    public List<HealthZoneResponse> listHealthZones() {
        return healthZoneRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public PageResponse<HealthZoneResponse> searchHealthZones(
            int page, int size, String query, String provinceCode) {
        return toPageResponse(
                healthZoneRepository.search(
                        normalizeFilter(query), normalizeFilter(provinceCode), pageRequest(page, size)),
                this::toResponse);
    }

    public List<HealthAreaResponse> listHealthAreas() {
        return healthAreaRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public PageResponse<HealthAreaResponse> searchHealthAreas(
            int page, int size, String query, String provinceCode) {
        return toPageResponse(
                healthAreaRepository.search(
                        normalizeFilter(query), normalizeFilter(provinceCode), pageRequest(page, size)),
                this::toResponse);
    }

    public List<HospitalResponse> listHospitals() {
        return hospitalRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public PageResponse<HospitalResponse> searchHospitals(
            int page, int size, String query, String provinceCode) {
        return toPageResponse(
                hospitalRepository.search(
                        normalizeFilter(query), normalizeFilter(provinceCode), pageRequest(page, size)),
                this::toResponse);
    }

    public HospitalAccessReferenceResponse resolveHospitalAccessReference(UUID hospitalId) {
        HospitalEntity hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new OrganizationNotFoundException("hôpital", hospitalId.toString()));
        List<HospitalLaboratoryEntity> hospitalLaboratories = hospitalLaboratoryRepository
                .findAllByHospital_IdAndActiveTrueOrderByNameAsc(hospitalId)
                .stream().toList();
        List<String> laboratoryCodes = hospitalLaboratories.stream().map(HospitalLaboratoryEntity::getCode).toList();
        List<HospitalLaboratoryAccessReference> laboratories = hospitalLaboratories.stream()
                .map(laboratory -> new HospitalLaboratoryAccessReference(laboratory.getCode(), laboratory.getName()))
                .toList();
        return new HospitalAccessReferenceResponse(
                hospital.getId(), hospital.getCode(), hospital.isActive(), laboratoryCodes, laboratories);
    }

    public List<ReferenceLaboratoryResponse> listReferenceLaboratories() {
        return referenceLaboratoryRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    /**
     * Returns the active provincial reference laboratories available to a hospital.
     * This is kept separate from the public laboratory registry because it is used
     * by internal workflows such as sending an analysis request to a reference laboratory.
     */
    public List<ReferenceLaboratoryAccessReference> listActiveReferenceLaboratoriesForHospital(UUID hospitalId) {
        HospitalEntity hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new OrganizationNotFoundException("hôpital", hospitalId.toString()));
        String provinceCode = hospital.getHealthZone().getProvince().getCode();
        return referenceLaboratoryRepository
                .findAllByProvince_CodeIgnoreCaseAndActiveTrueOrderByNameAsc(provinceCode)
                .stream()
                .map(referenceLaboratory -> new ReferenceLaboratoryAccessReference(
                        referenceLaboratory.getCode(), referenceLaboratory.getName()))
                .toList();
    }

    public PageResponse<ReferenceLaboratoryResponse> searchReferenceLaboratories(
            int page, int size, String query, String provinceCode) {
        return toPageResponse(
                referenceLaboratoryRepository.search(
                        normalizeFilter(query), normalizeFilter(provinceCode), pageRequest(page, size)),
                this::toResponse);
    }

    public List<HospitalLaboratoryResponse> listHospitalLaboratories() {
        return hospitalLaboratoryRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public PageResponse<HospitalLaboratoryResponse> searchHospitalLaboratories(int page, int size, String query) {
        return toPageResponse(hospitalLaboratoryRepository.search(normalizeFilter(query), pageRequest(page, size)), this::toResponse);
    }

    public List<LaboratoryStructureResponse> listLaboratoryStructures() {
        return laboratoryStructureRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public PageResponse<LaboratoryStructureResponse> searchLaboratoryStructures(int page, int size, String query) {
        return toPageResponse(laboratoryStructureRepository.search(normalizeFilter(query), pageRequest(page, size)), this::toResponse);
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
    public HealthAreaResponse updateHealthAreaStatus(String healthAreaCode, UpdateOrganizationStatusRequest request) {
        HealthAreaEntity healthArea = healthAreaRepository.findByCodeIgnoreCase(normalizeCode(healthAreaCode))
                .orElseThrow(() -> new OrganizationNotFoundException("L'aire de santé", healthAreaCode));
        healthArea.setActive(request.active());
        return toResponse(healthArea);
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
    public HospitalLaboratoryResponse updateHospitalLaboratoryStatus(
            String hospitalLaboratoryCode, UpdateOrganizationStatusRequest request) {
        HospitalLaboratoryEntity hospitalLaboratory = hospitalLaboratoryRepository
                .findByCodeIgnoreCase(normalizeCode(hospitalLaboratoryCode))
                .orElseThrow(() -> new OrganizationNotFoundException("Le laboratoire interne", hospitalLaboratoryCode));
        hospitalLaboratory.setActive(request.active());
        return toResponse(hospitalLaboratory);
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
    public HealthAreaResponse createHealthArea(CreateHealthAreaRequest request) {
        String code = normalizeCode(request.code());
        if (healthAreaRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateOrganizationException("L'aire de santé", code);
        }
        HealthZoneEntity healthZone = healthZoneRepository.findByCodeIgnoreCase(normalizeCode(request.healthZoneCode()))
                .orElseThrow(() -> new OrganizationNotFoundException("La zone de santé", request.healthZoneCode()));
        return toResponse(healthAreaRepository.save(new HealthAreaEntity(code, request.name().trim(), healthZone)));
    }

    @Transactional
    public HospitalResponse createHospital(CreateHospitalRequest request) {
        String code = normalizeCode(request.code());
        if (hospitalRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateOrganizationException("L'établissement", code);
        }
        HealthZoneEntity healthZone = healthZoneRepository.findByCodeIgnoreCase(normalizeCode(request.healthZoneCode()))
                .orElseThrow(() -> new OrganizationNotFoundException("La zone de santé", request.healthZoneCode()));
        HealthAreaEntity healthArea = null;
        String healthAreaCode = trimToNull(request.healthAreaCode());
        if (healthAreaCode != null) {
            healthArea = healthAreaRepository.findByCodeIgnoreCase(normalizeCode(healthAreaCode))
                    .orElseThrow(() -> new OrganizationNotFoundException("L'aire de santé", healthAreaCode));
            if (!healthArea.getHealthZone().getCode().equalsIgnoreCase(healthZone.getCode())) {
                throw new IllegalArgumentException("L'aire de santé doit appartenir à la zone de santé sélectionnée.");
            }
        } else if (request.type() == HospitalType.HEALTH_CENTER) {
            throw new IllegalArgumentException("Une aire de santé est obligatoire pour un centre de santé.");
        }
        HospitalEntity hospital = new HospitalEntity(
                UUID.randomUUID(),
                code,
                request.name().trim(),
                request.type(),
                healthZone,
                healthArea,
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
    public HospitalLaboratoryResponse createHospitalLaboratory(CreateHospitalLaboratoryRequest request) {
        String code = normalizeCode(request.code());
        if (hospitalLaboratoryRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateOrganizationException("Le laboratoire interne", code);
        }
        HospitalEntity hospital = hospitalRepository.findByCodeIgnoreCase(normalizeCode(request.hospitalCode()))
                .orElseThrow(() -> new OrganizationNotFoundException("L'établissement", request.hospitalCode()));
        HospitalLaboratoryEntity hospitalLaboratory = new HospitalLaboratoryEntity(
                UUID.randomUUID(),
                code,
                request.name().trim(),
                hospital,
                trimToNull(request.location()),
                trimToNull(request.phoneNumber()));
        return toResponse(hospitalLaboratoryRepository.save(hospitalLaboratory));
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

    private HealthAreaResponse toResponse(HealthAreaEntity healthArea) {
        HealthZoneEntity healthZone = healthArea.getHealthZone();
        return new HealthAreaResponse(
                healthArea.getCode(),
                healthArea.getName(),
                healthZone.getProvince().getCode(),
                healthZone.getCode(),
                healthArea.isActive());
    }

    private HospitalResponse toResponse(HospitalEntity hospital) {
        HealthZoneEntity healthZone = hospital.getHealthZone();
        HealthAreaEntity healthArea = hospital.getHealthArea();
        return new HospitalResponse(
                hospital.getId(),
                hospital.getCode(),
                hospital.getName(),
                hospital.getType(),
                healthZone.getProvince().getCode(),
                healthZone.getCode(),
                healthArea == null ? null : healthArea.getCode(),
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

    private HospitalLaboratoryResponse toResponse(HospitalLaboratoryEntity hospitalLaboratory) {
        HospitalEntity hospital = hospitalLaboratory.getHospital();
        return new HospitalLaboratoryResponse(
                hospitalLaboratory.getId(),
                hospitalLaboratory.getCode(),
                hospitalLaboratory.getName(),
                hospital.getCode(),
                hospital.getName(),
                hospitalLaboratory.getLocation(),
                hospitalLaboratory.getPhoneNumber(),
                hospitalLaboratory.isActive());
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

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(normalizePage(page), normalizePageSize(size), Sort.by("name").ascending());
    }

    private <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizePageSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private String normalizeFilter(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
