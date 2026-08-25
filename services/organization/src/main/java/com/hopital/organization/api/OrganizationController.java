package com.hopital.organization.api;

import com.hopital.organization.application.dto.CreateHealthZoneRequest;
import com.hopital.organization.application.dto.CreateHealthAreaRequest;
import com.hopital.organization.application.dto.CreateHospitalRequest;
import com.hopital.organization.application.dto.CreateHospitalLaboratoryRequest;
import com.hopital.organization.application.dto.CreateLaboratoryStructureRequest;
import com.hopital.organization.application.dto.CreateProvinceRequest;
import com.hopital.organization.application.dto.CreateReferenceLaboratoryRequest;
import com.hopital.organization.application.dto.HealthZoneResponse;
import com.hopital.organization.application.dto.HealthAreaResponse;
import com.hopital.organization.application.dto.HospitalResponse;
import com.hopital.organization.application.dto.HospitalLaboratoryResponse;
import com.hopital.organization.application.dto.LaboratoryStructureResponse;
import com.hopital.organization.application.dto.PageResponse;
import com.hopital.organization.application.dto.ProvinceResponse;
import com.hopital.organization.application.dto.ReferenceLaboratoryResponse;
import com.hopital.organization.application.dto.UpdateOrganizationStatusRequest;
import com.hopital.organization.application.service.OrganizationApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationApplicationService organizationApplicationService;

    public OrganizationController(OrganizationApplicationService organizationApplicationService) {
        this.organizationApplicationService = organizationApplicationService;
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceResponse>> listProvinces() {
        return ResponseEntity.ok(organizationApplicationService.listProvinces());
    }

    @GetMapping("/provinces/search")
    public ResponseEntity<PageResponse<ProvinceResponse>> searchProvinces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(organizationApplicationService.searchProvinces(page, size, query));
    }

    @PostMapping("/provinces")
    public ResponseEntity<ProvinceResponse> createProvince(@Valid @RequestBody CreateProvinceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationApplicationService.createProvince(request));
    }

    @PatchMapping("/provinces/{provinceCode}/status")
    public ResponseEntity<ProvinceResponse> updateProvinceStatus(
            @PathVariable("provinceCode") String provinceCode,
            @Valid @RequestBody UpdateOrganizationStatusRequest request) {
        return ResponseEntity.ok(organizationApplicationService.updateProvinceStatus(provinceCode, request));
    }

    @GetMapping("/health-zones")
    public ResponseEntity<List<HealthZoneResponse>> listHealthZones() {
        return ResponseEntity.ok(organizationApplicationService.listHealthZones());
    }

    @GetMapping("/health-zones/search")
    public ResponseEntity<PageResponse<HealthZoneResponse>> searchHealthZones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String provinceCode) {
        return ResponseEntity.ok(organizationApplicationService.searchHealthZones(page, size, query, provinceCode));
    }

    @PostMapping("/health-zones")
    public ResponseEntity<HealthZoneResponse> createHealthZone(@Valid @RequestBody CreateHealthZoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationApplicationService.createHealthZone(request));
    }

    @PatchMapping("/health-zones/{healthZoneCode}/status")
    public ResponseEntity<HealthZoneResponse> updateHealthZoneStatus(
            @PathVariable("healthZoneCode") String healthZoneCode,
            @Valid @RequestBody UpdateOrganizationStatusRequest request) {
        return ResponseEntity.ok(organizationApplicationService.updateHealthZoneStatus(healthZoneCode, request));
    }

    @GetMapping("/health-areas")
    public ResponseEntity<List<HealthAreaResponse>> listHealthAreas() {
        return ResponseEntity.ok(organizationApplicationService.listHealthAreas());
    }

    @GetMapping("/health-areas/search")
    public ResponseEntity<PageResponse<HealthAreaResponse>> searchHealthAreas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String provinceCode) {
        return ResponseEntity.ok(organizationApplicationService.searchHealthAreas(page, size, query, provinceCode));
    }

    @PostMapping("/health-areas")
    public ResponseEntity<HealthAreaResponse> createHealthArea(@Valid @RequestBody CreateHealthAreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationApplicationService.createHealthArea(request));
    }

    @PatchMapping("/health-areas/{healthAreaCode}/status")
    public ResponseEntity<HealthAreaResponse> updateHealthAreaStatus(
            @PathVariable("healthAreaCode") String healthAreaCode,
            @Valid @RequestBody UpdateOrganizationStatusRequest request) {
        return ResponseEntity.ok(organizationApplicationService.updateHealthAreaStatus(healthAreaCode, request));
    }

    @GetMapping("/hospitals")
    public ResponseEntity<List<HospitalResponse>> listHospitals() {
        return ResponseEntity.ok(organizationApplicationService.listHospitals());
    }

    @GetMapping("/hospitals/search")
    public ResponseEntity<PageResponse<HospitalResponse>> searchHospitals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String provinceCode) {
        return ResponseEntity.ok(organizationApplicationService.searchHospitals(page, size, query, provinceCode));
    }

    @PostMapping("/hospitals")
    public ResponseEntity<HospitalResponse> createHospital(@Valid @RequestBody CreateHospitalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationApplicationService.createHospital(request));
    }

    @PatchMapping("/hospitals/{hospitalCode}/status")
    public ResponseEntity<HospitalResponse> updateHospitalStatus(
            @PathVariable("hospitalCode") String hospitalCode,
            @Valid @RequestBody UpdateOrganizationStatusRequest request) {
        return ResponseEntity.ok(organizationApplicationService.updateHospitalStatus(hospitalCode, request));
    }

    @GetMapping("/reference-laboratories")
    public ResponseEntity<List<ReferenceLaboratoryResponse>> listReferenceLaboratories() {
        return ResponseEntity.ok(organizationApplicationService.listReferenceLaboratories());
    }

    @GetMapping("/reference-laboratories/search")
    public ResponseEntity<PageResponse<ReferenceLaboratoryResponse>> searchReferenceLaboratories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String provinceCode) {
        return ResponseEntity.ok(
                organizationApplicationService.searchReferenceLaboratories(page, size, query, provinceCode));
    }

    @PostMapping("/reference-laboratories")
    public ResponseEntity<ReferenceLaboratoryResponse> createReferenceLaboratory(
            @Valid @RequestBody CreateReferenceLaboratoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationApplicationService.createReferenceLaboratory(request));
    }

    @PatchMapping("/reference-laboratories/{referenceLaboratoryCode}/status")
    public ResponseEntity<ReferenceLaboratoryResponse> updateReferenceLaboratoryStatus(
            @PathVariable("referenceLaboratoryCode") String referenceLaboratoryCode,
            @Valid @RequestBody UpdateOrganizationStatusRequest request) {
        return ResponseEntity.ok(
                organizationApplicationService.updateReferenceLaboratoryStatus(referenceLaboratoryCode, request));
    }

    @GetMapping("/hospital-laboratories")
    public ResponseEntity<List<HospitalLaboratoryResponse>> listHospitalLaboratories() {
        return ResponseEntity.ok(organizationApplicationService.listHospitalLaboratories());
    }

    @GetMapping("/hospital-laboratories/search")
    public ResponseEntity<PageResponse<HospitalLaboratoryResponse>> searchHospitalLaboratories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(organizationApplicationService.searchHospitalLaboratories(page, size, query));
    }

    @PostMapping("/hospital-laboratories")
    public ResponseEntity<HospitalLaboratoryResponse> createHospitalLaboratory(
            @Valid @RequestBody CreateHospitalLaboratoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationApplicationService.createHospitalLaboratory(request));
    }

    @PatchMapping("/hospital-laboratories/{hospitalLaboratoryCode}/status")
    public ResponseEntity<HospitalLaboratoryResponse> updateHospitalLaboratoryStatus(
            @PathVariable("hospitalLaboratoryCode") String hospitalLaboratoryCode,
            @Valid @RequestBody UpdateOrganizationStatusRequest request) {
        return ResponseEntity.ok(organizationApplicationService.updateHospitalLaboratoryStatus(hospitalLaboratoryCode, request));
    }

    @GetMapping("/laboratory-structures")
    public ResponseEntity<List<LaboratoryStructureResponse>> listLaboratoryStructures() {
        return ResponseEntity.ok(organizationApplicationService.listLaboratoryStructures());
    }

    @GetMapping("/laboratory-structures/search")
    public ResponseEntity<PageResponse<LaboratoryStructureResponse>> searchLaboratoryStructures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(organizationApplicationService.searchLaboratoryStructures(page, size, query));
    }

    @PostMapping("/laboratory-structures")
    public ResponseEntity<LaboratoryStructureResponse> createLaboratoryStructure(
            @Valid @RequestBody CreateLaboratoryStructureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationApplicationService.createLaboratoryStructure(request));
    }

    @PatchMapping("/laboratory-structures/{laboratoryStructureCode}/status")
    public ResponseEntity<LaboratoryStructureResponse> updateLaboratoryStructureStatus(
            @PathVariable("laboratoryStructureCode") String laboratoryStructureCode,
            @Valid @RequestBody UpdateOrganizationStatusRequest request) {
        return ResponseEntity.ok(
                organizationApplicationService.updateLaboratoryStructureStatus(laboratoryStructureCode, request));
    }
}
