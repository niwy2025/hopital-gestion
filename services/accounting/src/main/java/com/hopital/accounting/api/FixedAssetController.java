package com.hopital.accounting.api;

import com.hopital.accounting.application.domain.AuditActor;
import com.hopital.accounting.application.dto.PageResponse;
import com.hopital.accounting.application.service.FixedAssetService;
import com.hopital.accounting.infra.integration.auth.AuthAccessScopeClient;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FixedAssetController {
    private static final String READ="hasAnyRole('ADMIN','HOSPITAL_ADMIN','HOSPITAL_ACCOUNTANT','FINANCE_MANAGER','FINANCE_AUDITOR')";
    private static final String WRITE="hasAnyRole('ADMIN','HOSPITAL_ACCOUNTANT','FINANCE_MANAGER')";
    private final FixedAssetService service; private final AuthAccessScopeClient scopes;
    public FixedAssetController(FixedAssetService service,AuthAccessScopeClient scopes) { this.service=service;this.scopes=scopes; }
    @PostMapping("/internal/accounting/patrimony-assets/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receive(@PathVariable("id") UUID id) { service.importAsset(id); }
    @GetMapping("/api/v1/accounting/fixed-assets/search") @PreAuthorize(READ)
    public PageResponse<Map<String,Object>> search(@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,
            @RequestParam(name="query",defaultValue="") String query,@RequestParam(name="status",defaultValue="") String status,
            @RequestParam(name="hospitalId",required=false) UUID hospital,@AuthenticationPrincipal Jwt jwt) {
        return service.search(page,size,query,status,hospital,scopes.resolve(jwt.getClaimAsString("preferred_username")));
    }
    @GetMapping("/api/v1/accounting/fixed-assets/{id}") @PreAuthorize(READ)
    public Map<String,Object> get(@PathVariable("id") UUID id,@AuthenticationPrincipal Jwt jwt) { return service.get(id,scopes.resolve(jwt.getClaimAsString("preferred_username"))); }
    @PostMapping("/api/v1/accounting/fixed-assets/{id}/classification") @PreAuthorize(WRITE)
    public Map<String,Object> classify(@PathVariable("id") UUID id,@Valid @RequestBody FixedAssetService.Classification r,@AuthenticationPrincipal Jwt jwt) { return service.classify(id,r,scopes.resolve(jwt.getClaimAsString("preferred_username")),new AuditActor(jwt.getSubject(),jwt.getClaimAsString("preferred_username"))); }
    @GetMapping("/api/v1/accounting/fixed-assets/{id}/schedule") @PreAuthorize(READ)
    public PageResponse<Map<String,Object>> schedule(@PathVariable("id") UUID id,@RequestParam(name="page",defaultValue="0") int page,
            @RequestParam(name="size",defaultValue="20") int size,@AuthenticationPrincipal Jwt jwt) { return service.schedule(id,page,size,scopes.resolve(jwt.getClaimAsString("preferred_username"))); }
    @PostMapping("/api/v1/accounting/fixed-assets/{id}/installments") @PreAuthorize(WRITE)
    public Map<String,Object> prepare(@PathVariable("id") UUID id,@Valid @RequestBody FixedAssetService.Installment r,@AuthenticationPrincipal Jwt jwt) { return service.prepareInstallment(id,r,scopes.resolve(jwt.getClaimAsString("preferred_username")),new AuditActor(jwt.getSubject(),jwt.getClaimAsString("preferred_username"))); }
}
