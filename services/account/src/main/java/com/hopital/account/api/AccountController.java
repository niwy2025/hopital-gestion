package com.hopital.account.api;

import com.hopital.account.application.dto.AccountResponse;
import com.hopital.account.application.dto.AccountDetailsResponse;
import com.hopital.account.application.dto.CreateAccountRequest;
import com.hopital.account.application.dto.PageResponse;
import com.hopital.account.application.dto.RoleResponse;
import com.hopital.account.application.dto.UpdateAccountRequest;
import com.hopital.account.application.dto.UpdateOwnAccountRequest;
import com.hopital.account.application.service.AccountApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountApplicationService accountApplicationService;

    public AccountController(AccountApplicationService accountApplicationService) {
        this.accountApplicationService = accountApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAccounts() {
        return ResponseEntity.ok(accountApplicationService.listAccounts());
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.ok(accountApplicationService.createAccount(request));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<AccountResponse>> searchAccounts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "hospitalId", required = false) String hospitalId) {
        return ResponseEntity.ok(accountApplicationService.searchAccounts(page, size, query, hospitalId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDetailsResponse> findById(@PathVariable("accountId") UUID accountId) {
        return ResponseEntity.ok(accountApplicationService.findById(accountId));
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountDetailsResponse> updateAccount(
            @PathVariable("accountId") UUID accountId,
            @Valid @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(accountApplicationService.updateAccount(accountId, request));
    }

    @GetMapping("/identifier/{identifier}")
    public ResponseEntity<AccountResponse> findByIdentifier(@PathVariable("identifier") String identifier) {
        return ResponseEntity.ok(accountApplicationService.findByIdentifier(identifier));
    }

    @GetMapping("/roles")
    public ResponseEntity<Set<RoleResponse>> listRoles() {
        return ResponseEntity.ok(accountApplicationService.listRoles());
    }

    @GetMapping("/me")
    public ResponseEntity<AccountDetailsResponse> findOwnAccount(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(accountApplicationService.findOwnAccount(username(jwt)));
    }

    @PutMapping("/me")
    public ResponseEntity<AccountDetailsResponse> updateOwnAccount(
            @Valid @RequestBody UpdateOwnAccountRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(accountApplicationService.updateOwnAccount(username(jwt), request));
    }

    private String username(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Le jeton ne contient pas d’identifiant utilisateur.");
        }
        return username;
    }
}
