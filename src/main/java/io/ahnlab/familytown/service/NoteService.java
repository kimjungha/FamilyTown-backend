package io.ahnlab.familytown.service;

import io.ahnlab.familytown.dto.NotePositionRequest;
import io.ahnlab.familytown.dto.NoteRequest;
import io.ahnlab.familytown.dto.NoteResponse;
import io.ahnlab.familytown.entity.Note;
import io.ahnlab.familytown.entity.Profile;
import io.ahnlab.familytown.repository.NoteRepository;
import io.ahnlab.familytown.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotes() {
        List<Note> notes = noteRepository.findAllByOrderByCreatedAtDesc();

        List<UUID> authorIds = notes.stream()
                .map(Note::getAuthorId)
                .distinct()
                .toList();

        Map<UUID, Profile> profileMap = profileRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Profile::getId, p -> p));

        return notes.stream()
                .map(note -> {
                    Profile profile = profileMap.get(note.getAuthorId());
                    return new NoteResponse(
                            note.getId(),
                            note.getAuthorId(),
                            profile != null ? profile.getNickname() : null,
                            profile != null ? profile.getAvatar() : null,
                            note.getContent(),
                            note.getColor(),
                            note.getPosX(),
                            note.getPosY(),
                            note.getRotation(),
                            note.getCreatedAt()
                    );
                })
                .toList();
    }

    @Transactional
    public NoteResponse createNote(UUID authorId, NoteRequest req) {
        Note note = new Note();
        note.setAuthorId(authorId);
        note.setContent(req.content());
        note.setColor(req.color());
        note.setPosX(req.posX());
        note.setPosY(req.posY());
        note.setRotation(req.rotation());
        note.setCreatedAt(OffsetDateTime.now());
        note.setUpdatedAt(OffsetDateTime.now());

        Note saved = noteRepository.save(note);

        Profile profile = profileRepository.findById(authorId).orElse(null);

        return new NoteResponse(
                saved.getId(),
                saved.getAuthorId(),
                profile != null ? profile.getNickname() : null,
                profile != null ? profile.getAvatar() : null,
                saved.getContent(),
                saved.getColor(),
                saved.getPosX(),
                saved.getPosY(),
                saved.getRotation(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public NoteResponse updatePosition(UUID noteId, UUID requesterId, NotePositionRequest req) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        if (!note.getAuthorId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the note owner");
        }

        note.setPosX(req.posX());
        note.setPosY(req.posY());
        note.setUpdatedAt(OffsetDateTime.now());

        Note saved = noteRepository.save(note);
        Profile profile = profileRepository.findById(saved.getAuthorId()).orElse(null);

        return new NoteResponse(
                saved.getId(),
                saved.getAuthorId(),
                profile != null ? profile.getNickname() : null,
                profile != null ? profile.getAvatar() : null,
                saved.getContent(),
                saved.getColor(),
                saved.getPosX(),
                saved.getPosY(),
                saved.getRotation(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public void deleteNote(UUID noteId, UUID requesterId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        if (!note.getAuthorId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the note owner");
        }

        noteRepository.delete(note);
    }
}
