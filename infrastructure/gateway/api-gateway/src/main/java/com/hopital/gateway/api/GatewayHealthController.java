package com.hopital.gateway.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayHealthController {

    @GetMapping("/gateway/ready")
    Map<String, String> ready() {
        return Map.of("status", "UP");
    }
}
