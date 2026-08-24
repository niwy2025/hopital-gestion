package com.hopital.notification.api;

import com.hopital.notification.application.dto.BroadcastAcceptedResponse;
import com.hopital.notification.application.dto.BroadcastRequest;
import com.hopital.notification.application.service.BroadcastApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class BroadcastController {

    private final BroadcastApplicationService broadcastApplicationService;

    public BroadcastController(BroadcastApplicationService broadcastApplicationService) {
        this.broadcastApplicationService = broadcastApplicationService;
    }

    @PostMapping("/broadcasts")
    public ResponseEntity<BroadcastAcceptedResponse> queue(@Valid @RequestBody BroadcastRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(broadcastApplicationService.queue(request));
    }
}
