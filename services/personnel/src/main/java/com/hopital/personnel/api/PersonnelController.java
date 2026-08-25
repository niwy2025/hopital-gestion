package com.hopital.personnel.api;

import com.hopital.personnel.application.dto.CreatePersonnelRequest;
import com.hopital.personnel.application.dto.PageResponse;
import com.hopital.personnel.application.dto.PersonnelResponse;
import com.hopital.personnel.application.dto.UpdatePersonnelRequest;
import com.hopital.personnel.application.dto.UpdatePersonnelStatusRequest;
import com.hopital.personnel.application.service.PersonnelApplicationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/personnel")
public class PersonnelController {

    private final PersonnelApplicationService personnelApplicationService;

    public PersonnelController(PersonnelApplicationService personnelApplicationService) {
        this.personnelApplicationService = personnelApplicationService;
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<PersonnelResponse>> searchPersonnel(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "hospitalId", required = false) String hospitalId,
            @RequestParam(name = "active", required = false) Boolean active) {
        return ResponseEntity.ok(personnelApplicationService.searchPersonnel(page, size, query, hospitalId, active));
    }

    @GetMapping("/{personnelId}")
    public ResponseEntity<PersonnelResponse> findById(@PathVariable("personnelId") UUID personnelId) {
        return ResponseEntity.ok(personnelApplicationService.findById(personnelId));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<PersonnelResponse> findByAccountId(@PathVariable("accountId") UUID accountId) {
        return ResponseEntity.ok(personnelApplicationService.findByAccountId(accountId));
    }

    @PostMapping
    public ResponseEntity<PersonnelResponse> createPersonnel(@Valid @RequestBody CreatePersonnelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personnelApplicationService.createPersonnel(request));
    }

    @PutMapping("/{personnelId}")
    public ResponseEntity<PersonnelResponse> updatePersonnel(
            @PathVariable("personnelId") UUID personnelId,
            @Valid @RequestBody UpdatePersonnelRequest request) {
        return ResponseEntity.ok(personnelApplicationService.updatePersonnel(personnelId, request));
    }

    @PatchMapping("/{personnelId}/status")
    public ResponseEntity<PersonnelResponse> updateStatus(
            @PathVariable("personnelId") UUID personnelId,
            @Valid @RequestBody UpdatePersonnelStatusRequest request) {
        return ResponseEntity.ok(personnelApplicationService.updateStatus(personnelId, request.active()));
    }
}
