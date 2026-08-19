package com.hopital.account.api;

import com.hopital.account.application.dto.AccountResponse;
import com.hopital.account.application.dto.CreateAccountRequest;
import com.hopital.account.application.dto.RoleResponse;
import com.hopital.account.application.service.AccountApplicationService;
import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        return ResponseEntity.ok(accountApplicationService.createAccount(request));
    }

    @GetMapping("/identifier/{identifier}")
    public ResponseEntity<AccountResponse> findByIdentifier(@PathVariable String identifier) {
        return ResponseEntity.ok(accountApplicationService.findByIdentifier(identifier));
    }

    @GetMapping("/roles")
    public ResponseEntity<Set<RoleResponse>> listRoles() {
        return ResponseEntity.ok(accountApplicationService.listRoles());
    }
}
