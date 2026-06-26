package io.ahnlab.familytown.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "leaving_work_events", schema = "public")
public class LeaveWorkEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
