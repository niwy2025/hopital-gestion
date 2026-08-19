package com.hopital.auth.api;

import com.hopital.auth.application.dto.LoginRequest;
import com.hopital.auth.application.dto.LoginResponse;
import com.hopital.auth.application.service.AuthApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authApplicationService.login(request));
    }

    @GetMapping("/health")
    ResponseEntity<String> health() {
        return ResponseEntity.ok("auth-service-ready");
    }
}
