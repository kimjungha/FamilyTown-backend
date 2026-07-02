package io.ahnlab.familytown.controller;

import io.ahnlab.familytown.dto.NotePositionRequest;
import io.ahnlab.familytown.dto.NoteRequest;
import io.ahnlab.familytown.dto.NoteResponse;
import io.ahnlab.familytown.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@RequestBody NoteRequest request,
                                                   HttpServletRequest httpRequest) {
        UUID userId = resolveUserId(httpRequest);
        NoteResponse response = noteService.createNote(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/position")
    public ResponseEntity<NoteResponse> updatePosition(@PathVariable UUID id,
                                                        @RequestBody NotePositionRequest request,
                                                        HttpServletRequest httpRequest) {
        UUID userId = resolveUserId(httpRequest);
        NoteResponse response = noteService.updatePosition(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable UUID id,
                                           HttpServletRequest httpRequest) {
        UUID userId = resolveUserId(httpRequest);
        noteService.deleteNote(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveUserId(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return UUID.fromString(userIdAttr.toString());
    }
}
