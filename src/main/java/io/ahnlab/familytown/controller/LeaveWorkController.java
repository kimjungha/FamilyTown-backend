package io.ahnlab.familytown.controller;

import io.ahnlab.familytown.service.LeaveWorkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveWorkController {

    private final LeaveWorkService leaveWorkService;

    @PostMapping
    public ResponseEntity<Void> recordLeave(HttpServletRequest httpRequest) {
        UUID userId = resolveUserId(httpRequest);
        leaveWorkService.recordAndNotify(userId);
        return ResponseEntity.ok().build();
    }

    private UUID resolveUserId(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return UUID.fromString(userIdAttr.toString());
    }
}
