package com.hopital.patient.api;

import com.hopital.patient.application.domain.AuditActor;
import com.hopital.patient.application.domain.DataAccessScope;
import com.hopital.patient.application.domain.PartnerCompanyDocumentType;
import com.hopital.patient.application.dto.CreatePartnerCompanyDocumentRequest;
import com.hopital.patient.application.dto.CreatePartnerCompanyMemberRequest;
import com.hopital.patient.application.dto.CreatePartnerCompanyRequest;
import com.hopital.patient.application.dto.PageResponse;
import com.hopital.patient.application.dto.PartnerCompanyDocumentResponse;
import com.hopital.patient.application.dto.PartnerCompanyMemberResponse;
import com.hopital.patient.application.dto.PartnerCompanyResponse;
import com.hopital.patient.application.dto.UpdatePartnerCompanyMemberRequest;
import com.hopital.patient.application.dto.UpdatePartnerCompanyMemberStatusRequest;
import com.hopital.patient.application.dto.UpdatePartnerCompanyRequest;
import com.hopital.patient.application.dto.UpdatePartnerCompanyStatusRequest;
import com.hopital.patient.application.service.PartnerCompanyApplicationService;
import com.hopital.patient.infra.integration.auth.AuthAccessScopeClient;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/patients/partner-companies")
public class PartnerCompanyController {

    private final PartnerCompanyApplicationService partnerCompanyApplicationService;
    private final AuthAccessScopeClient authAccessScopeClient;

    public PartnerCompanyController(
            PartnerCompanyApplicationService partnerCompanyApplicationService,
            AuthAccessScopeClient authAccessScopeClient) {
        this.partnerCompanyApplicationService = partnerCompanyApplicationService;
        this.authAccessScopeClient = authAccessScopeClient;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PartnerCompanyResponse>> searchCompanies(
            @RequestParam(name = "hospitalId") UUID hospitalId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "active", required = false) Boolean active,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(partnerCompanyApplicationService.searchCompanies(
                hospitalId, query, active, page, size, accessScope(jwt)));
    }

    @PostMapping
    public ResponseEntity<PartnerCompanyResponse> createCompany(
            @Valid @RequestBody CreatePartnerCompanyRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerCompanyApplicationService.createCompany(
                request, accessScope(jwt), auditActor(jwt)));
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<PartnerCompanyResponse> getCompany(
            @PathVariable("companyId") UUID companyId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(partnerCompanyApplicationService.getCompany(companyId, accessScope(jwt)));
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<PartnerCompanyResponse> updateCompany(
            @PathVariable("companyId") UUID companyId,
            @Valid @RequestBody UpdatePartnerCompanyRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(partnerCompanyApplicationService.updateCompany(
                companyId, request, accessScope(jwt), auditActor(jwt)));
    }

    @PatchMapping("/{companyId}/status")
    public ResponseEntity<PartnerCompanyResponse> updateCompanyStatus(
            @PathVariable("companyId") UUID companyId,
            @Valid @RequestBody UpdatePartnerCompanyStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(partnerCompanyApplicationService.updateCompanyStatus(
                companyId, request, accessScope(jwt), auditActor(jwt)));
    }

    @GetMapping("/{companyId}/members")
    public ResponseEntity<PageResponse<PartnerCompanyMemberResponse>> searchMembers(
            @PathVariable("companyId") UUID companyId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "sponsorMemberId", required = false) UUID sponsorMemberId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(partnerCompanyApplicationService.searchMembers(
                companyId, query, active, sponsorMemberId, page, size, accessScope(jwt)));
    }

    @GetMapping("/{companyId}/documents")
    public ResponseEntity<PageResponse<PartnerCompanyDocumentResponse>> searchDocuments(
            @PathVariable("companyId") UUID companyId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "documentType", required = false) PartnerCompanyDocumentType documentType,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(partnerCompanyApplicationService.searchDocuments(
                companyId, query, documentType, page, size, accessScope(jwt)));
    }

    @PostMapping("/{companyId}/documents")
    public ResponseEntity<PartnerCompanyDocumentResponse> addDocument(
            @PathVariable("companyId") UUID companyId,
            @Valid @RequestBody CreatePartnerCompanyDocumentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerCompanyApplicationService.addDocument(
                companyId, request, accessScope(jwt), auditActor(jwt)));
    }

    @DeleteMapping("/{companyId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("documentId") UUID documentId,
            @AuthenticationPrincipal Jwt jwt) {
        partnerCompanyApplicationService.deleteDocument(companyId, documentId, accessScope(jwt));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{companyId}/members")
    public ResponseEntity<PartnerCompanyMemberResponse> createMember(
            @PathVariable("companyId") UUID companyId,
            @Valid @RequestBody CreatePartnerCompanyMemberRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerCompanyApplicationService.createMember(
                companyId, request, accessScope(jwt), auditActor(jwt)));
    }

    @GetMapping("/{companyId}/members/{memberId}")
    public ResponseEntity<PartnerCompanyMemberResponse> getMember(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("memberId") UUID memberId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(partnerCompanyApplicationService.getMember(companyId, memberId, accessScope(jwt)));
    }

    @PutMapping("/{companyId}/members/{memberId}")
    public ResponseEntity<PartnerCompanyMemberResponse> updateMember(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("memberId") UUID memberId,
            @Valid @RequestBody UpdatePartnerCompanyMemberRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(partnerCompanyApplicationService.updateMember(
                companyId, memberId, request, accessScope(jwt), auditActor(jwt)));
    }

    @PatchMapping("/{companyId}/members/{memberId}/status")
    public ResponseEntity<PartnerCompanyMemberResponse> updateMemberStatus(
            @PathVariable("companyId") UUID companyId,
            @PathVariable("memberId") UUID memberId,
            @Valid @RequestBody UpdatePartnerCompanyMemberStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(partnerCompanyApplicationService.updateMemberStatus(
                companyId, memberId, request, accessScope(jwt), auditActor(jwt)));
    }

    private DataAccessScope accessScope(Jwt jwt) {
        return authAccessScopeClient.resolve(jwt.getClaimAsString("preferred_username"));
    }

    private AuditActor auditActor(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return new AuditActor(
                jwt.getSubject(),
                username == null || username.isBlank() ? "Utilisateur authentifié" : username);
    }
}
