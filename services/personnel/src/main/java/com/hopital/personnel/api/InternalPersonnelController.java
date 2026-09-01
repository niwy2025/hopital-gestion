package com.hopital.personnel.api;

import com.hopital.personnel.application.dto.PersonnelAccessScopeResponse;
import com.hopital.personnel.application.dto.PersonnelCareReferenceResponse;
import com.hopital.personnel.application.service.PersonnelApplicationService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal API, intentionally not routed through Kong. */
@RestController
@RequestMapping("/internal/personnel")
public class InternalPersonnelController {

    private final PersonnelApplicationService personnelApplicationService;

    public InternalPersonnelController(PersonnelApplicationService personnelApplicationService) {
        this.personnelApplicationService = personnelApplicationService;
    }

    @GetMapping("/accounts/{accountId}/access-scope")
    public ResponseEntity<PersonnelAccessScopeResponse> resolveAccessScope(@PathVariable("accountId") UUID accountId) {
        return ResponseEntity.ok(personnelApplicationService.resolveAccessScope(accountId));
    }

    @GetMapping("/{personnelId}/hospitals/{hospitalId}/care-reference")
    public ResponseEntity<PersonnelCareReferenceResponse> resolveCarePersonnel(
            @PathVariable("personnelId") UUID personnelId,
            @PathVariable("hospitalId") UUID hospitalId) {
        return ResponseEntity.ok(personnelApplicationService.resolveCarePersonnel(personnelId, hospitalId));
    }
}
