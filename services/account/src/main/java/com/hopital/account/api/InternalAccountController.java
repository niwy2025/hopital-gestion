package com.hopital.account.api;

import com.hopital.account.application.dto.AuthenticatedAccountResponse;
import com.hopital.account.application.dto.CredentialsValidationRequest;
import com.hopital.account.application.service.AccountApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/accounts")
public class InternalAccountController {

    private final AccountApplicationService accountApplicationService;

    public InternalAccountController(AccountApplicationService accountApplicationService) {
        this.accountApplicationService = accountApplicationService;
    }

    @PostMapping("/validate-credentials")
    public ResponseEntity<AuthenticatedAccountResponse> validateCredentials(@RequestBody CredentialsValidationRequest request) {
        return ResponseEntity.ok(accountApplicationService.validateCredentials(request));
    }
}
