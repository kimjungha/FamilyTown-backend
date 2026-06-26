package io.ahnlab.familytown.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notes", schema = "public")
public class Note {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "content")
    private String content;

    @Column(name = "color")
    private String color;

    @Column(name = "pos_x")
    private BigDecimal posX;

    @Column(name = "pos_y")
    private BigDecimal posY;

    @Column(name = "rotation")
    private BigDecimal rotation;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
