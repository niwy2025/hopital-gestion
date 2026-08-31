package com.hopital.organization.api;

import com.hopital.organization.application.dto.HospitalAccessReferenceResponse;
import com.hopital.organization.application.service.OrganizationApplicationService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal API, intentionally excluded from the public gateway routes. */
@RestController
@RequestMapping("/internal/organizations")
public class InternalOrganizationController {

    private final OrganizationApplicationService organizationApplicationService;

    public InternalOrganizationController(OrganizationApplicationService organizationApplicationService) {
        this.organizationApplicationService = organizationApplicationService;
    }

    @GetMapping("/hospitals/{hospitalId}/access-reference")
    public ResponseEntity<HospitalAccessReferenceResponse> resolveHospital(@PathVariable("hospitalId") UUID hospitalId) {
        return ResponseEntity.ok(organizationApplicationService.resolveHospitalAccessReference(hospitalId));
    }
}
