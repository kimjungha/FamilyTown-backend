package io.ahnlab.familytown.controller;

import io.ahnlab.familytown.dto.PushSubscribeRequest;
import io.ahnlab.familytown.service.WebPushService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {

    private final WebPushService webPushService;

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestBody PushSubscribeRequest request,
                                          HttpServletRequest httpRequest) {
        UUID userId = resolveUserId(httpRequest);
        webPushService.subscribe(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private UUID resolveUserId(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return UUID.fromString(userIdAttr.toString());
    }
}
