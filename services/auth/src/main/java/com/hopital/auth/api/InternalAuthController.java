package com.hopital.auth.api;

import com.hopital.auth.application.dto.DataAccessScopeResponse;
import com.hopital.auth.application.service.AuthApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal API, never routed by Kong. */
@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private final AuthApplicationService authApplicationService;

    public InternalAuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @GetMapping("/access-scopes/{username}")
    ResponseEntity<DataAccessScopeResponse> resolveDataAccessScope(@PathVariable String username) {
        return ResponseEntity.ok(authApplicationService.resolveDataAccessScope(username));
    }
}
