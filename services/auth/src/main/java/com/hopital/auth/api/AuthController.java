package com.hopital.auth.api;

import com.hopital.auth.application.dto.LoginRequest;
import com.hopital.auth.application.dto.LoginResponse;
import com.hopital.auth.application.dto.RefreshTokenRequest;
import com.hopital.auth.application.service.AuthApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            @RequestHeader(value = "User-Agent", defaultValue = "unknown") String userAgent) {
        return ResponseEntity.ok(authApplicationService.login(withUserAgent(request, userAgent)));
    }

    @PostMapping("/refresh")
    ResponseEntity<LoginResponse> refresh(
            @RequestBody RefreshTokenRequest request,
            @RequestHeader(value = "User-Agent", defaultValue = "unknown") String userAgent) {
        return ResponseEntity.ok(authApplicationService.refresh(withUserAgent(request, userAgent)));
    }

    @GetMapping("/health")
    ResponseEntity<String> health() {
        return ResponseEntity.ok("auth-service-ready");
    }

    private LoginRequest withUserAgent(LoginRequest request, String requestUserAgent) {
        return new LoginRequest(request.username(), request.password(), resolveUserAgent(request.userAgent(), requestUserAgent));
    }

    private RefreshTokenRequest withUserAgent(RefreshTokenRequest request, String requestUserAgent) {
        return new RefreshTokenRequest(request.refreshToken(), resolveUserAgent(request.userAgent(), requestUserAgent));
    }

    private String resolveUserAgent(String suppliedUserAgent, String requestUserAgent) {
        if (requestUserAgent != null && !requestUserAgent.isBlank() && !"unknown".equalsIgnoreCase(requestUserAgent)) {
            return requestUserAgent;
        }
        return suppliedUserAgent == null || suppliedUserAgent.isBlank() ? "unknown" : suppliedUserAgent;
    }
}
