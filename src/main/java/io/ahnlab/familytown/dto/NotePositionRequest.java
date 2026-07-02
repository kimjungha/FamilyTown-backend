package io.ahnlab.familytown.dto;

import java.math.BigDecimal;

public record NotePositionRequest(
        BigDecimal posX,
        BigDecimal posY
) {
}
