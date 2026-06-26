package io.ahnlab.familytown.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NoteResponse(
        UUID id,
        UUID authorId,
        String authorNickname,
        String authorAvatar,
        String content,
        String color,
        BigDecimal posX,
        BigDecimal posY,
        BigDecimal rotation,
        OffsetDateTime createdAt
) {
}
