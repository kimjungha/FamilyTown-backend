package io.ahnlab.familytown.repository;

import io.ahnlab.familytown.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, UUID> {

    List<PushSubscription> findAllByUserIdNot(UUID userId);

    Optional<PushSubscription> findByEndpoint(String endpoint);
}
