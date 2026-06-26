package io.ahnlab.familytown.dto;

import java.math.BigDecimal;

public record NoteRequest(
        String content,
        String color,
        BigDecimal posX,
        BigDecimal posY,
        BigDecimal rotation
) {
}
