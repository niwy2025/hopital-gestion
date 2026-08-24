package com.hopital.organization.api;

import com.hopital.organization.application.dto.CreateHealthZoneRequest;
import com.hopital.organization.application.dto.CreateHospitalRequest;
import com.hopital.organization.application.dto.CreateProvinceRequest;
import com.hopital.organization.application.dto.HealthZoneResponse;
import com.hopital.organization.application.dto.HospitalResponse;
import com.hopital.organization.application.dto.ProvinceResponse;
import com.hopital.organization.application.service.OrganizationApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping("/provinces")
    public ResponseEntity<ProvinceResponse> createProvince(@Valid @RequestBody CreateProvinceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationApplicationService.createProvince(request));
    }

    @GetMapping("/health-zones")
    public ResponseEntity<List<HealthZoneResponse>> listHealthZones() {
        return ResponseEntity.ok(organizationApplicationService.listHealthZones());
    }

    @PostMapping("/health-zones")
    public ResponseEntity<HealthZoneResponse> createHealthZone(@Valid @RequestBody CreateHealthZoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationApplicationService.createHealthZone(request));
    }

    @GetMapping("/hospitals")
    public ResponseEntity<List<HospitalResponse>> listHospitals() {
        return ResponseEntity.ok(organizationApplicationService.listHospitals());
    }

    @PostMapping("/hospitals")
    public ResponseEntity<HospitalResponse> createHospital(@Valid @RequestBody CreateHospitalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationApplicationService.createHospital(request));
    }
}
