package com.hopital.accounting.billing;

import com.hopital.accounting.application.domain.*;
import com.hopital.accounting.infra.integration.auth.AuthAccessScopeClient;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounting/billing")
public class BillingController {
    private static final String READ="hasAnyRole('ADMIN','HOSPITAL_ADMIN','HOSPITAL_ACCOUNTANT','FINANCE_MANAGER','FINANCE_AUDITOR','BILLING_OFFICER','CASHIER')";
    private static final String CONFIG="hasAnyRole('ADMIN','FINANCE_MANAGER')";
    private static final String WRITE="hasAnyRole('ADMIN','FINANCE_MANAGER','BILLING_OFFICER')";
    private final BillingService service;private final AuthAccessScopeClient scopes;
    public BillingController(BillingService service,AuthAccessScopeClient scopes){this.service=service;this.scopes=scopes;}
    private DataAccessScope scope(Jwt jwt){return scopes.resolve(jwt.getClaimAsString("preferred_username"));}
    private AuditActor actor(Jwt jwt){return new AuditActor(jwt.getSubject(),jwt.getClaimAsString("preferred_username"));}
    @GetMapping("/services/search") @PreAuthorize(READ)
    public Object search(@RequestParam(name="hospitalId",required=false) UUID hospital,@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,
        @RequestParam(name="query",defaultValue="") String query,@RequestParam(name="category",defaultValue="") String category,@RequestParam(name="active",required=false) Boolean active,@AuthenticationPrincipal Jwt jwt){return service.services(hospital,page,size,query,category,active,scope(jwt));}
    @PostMapping("/services") @PreAuthorize(CONFIG)
    public Object create(@Valid @RequestBody BillingRequests.Service r,@AuthenticationPrincipal Jwt jwt){return service.create(r,scope(jwt),actor(jwt));}
    @GetMapping("/services/{id}") @PreAuthorize(READ)
    public Object get(@PathVariable("id") UUID id,@AuthenticationPrincipal Jwt jwt){return service.service(id,scope(jwt));}
    @PutMapping("/services/{id}") @PreAuthorize(CONFIG)
    public Object update(@PathVariable("id") UUID id,@Valid @RequestBody BillingRequests.UpdateService r,@AuthenticationPrincipal Jwt jwt){return service.update(id,r,scope(jwt),actor(jwt));}
    @GetMapping("/services/{id}/tariffs/search") @PreAuthorize(READ)
    public Object tariffs(@PathVariable("id") UUID id,@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@AuthenticationPrincipal Jwt jwt){return service.tariffs(id,page,size,scope(jwt));}
    @GetMapping("/services/{id}/events/search") @PreAuthorize(READ)
    public Object events(@PathVariable("id") UUID id,@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@AuthenticationPrincipal Jwt jwt){return service.events(id,page,size,scope(jwt));}
    @PostMapping("/services/{id}/tariffs") @PreAuthorize(CONFIG)
    public Object tariff(@PathVariable("id") UUID id,@Valid @RequestBody BillingRequests.Tariff r,@AuthenticationPrincipal Jwt jwt){return service.tariff(id,r,scope(jwt),actor(jwt));}
    @PostMapping("/services/{id}/tariffs/{tariff}/cancel") @PreAuthorize(CONFIG)
    public Object cancelTariff(@PathVariable("id") UUID id,@PathVariable("tariff") UUID tariff,@Valid @RequestBody BillingRequests.Note r,@AuthenticationPrincipal Jwt jwt){return service.cancelTariff(id,tariff,r.reason(),scope(jwt),actor(jwt));}
    @GetMapping("/passages/search") @PreAuthorize(READ)
    public Object passages(@RequestParam(name="hospitalId",required=false) UUID hospital,@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam(name="query",defaultValue="") String query,@AuthenticationPrincipal Jwt jwt){return service.passages(hospital,page,size,query,scope(jwt));}
    @GetMapping("/passages/{id}") @PreAuthorize(READ)
    public Object workspace(@PathVariable("id") UUID id,@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="20") int size,@RequestParam(name="query",defaultValue="") String query,@RequestParam(name="status",defaultValue="") String status,@AuthenticationPrincipal Jwt jwt){return service.workspace(id,page,size,query,status,scope(jwt));}
    @GetMapping("/passages/{id}/quote") @PreAuthorize(WRITE)
    public Object quote(@PathVariable("id") UUID id,@RequestParam("serviceId") UUID serviceId,@RequestParam("performedOn") LocalDate date,@RequestParam("quantity") BigDecimal quantity,@RequestParam("currency") String currency,@AuthenticationPrincipal Jwt jwt){return service.quote(id,serviceId,date,quantity,currency,scope(jwt));}
    @PostMapping("/passages/{id}/charges") @PreAuthorize(WRITE)
    public Object charge(@PathVariable("id") UUID id,@Valid @RequestBody BillingRequests.Charge r,@AuthenticationPrincipal Jwt jwt){return service.charge(id,r,scope(jwt),actor(jwt));}
    @PostMapping("/passages/{id}/charges/{charge}/cancel") @PreAuthorize(WRITE)
    public Object cancelCharge(@PathVariable("id") UUID id,@PathVariable("charge") UUID charge,@Valid @RequestBody BillingRequests.Note r,@AuthenticationPrincipal Jwt jwt){return service.cancelCharge(id,charge,r.reason(),scope(jwt),actor(jwt));}
}
