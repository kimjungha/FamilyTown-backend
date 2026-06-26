package io.ahnlab.familytown.service;

import io.ahnlab.familytown.config.AppProperties;
import io.ahnlab.familytown.dto.PushSubscribeRequest;
import io.ahnlab.familytown.entity.PushSubscription;
import io.ahnlab.familytown.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Transactional
    public void subscribe(UUID userId, PushSubscribeRequest req) {
        PushSubscription subscription = pushSubscriptionRepository
                .findByEndpoint(req.endpoint())
                .orElseGet(PushSubscription::new);

        subscription.setUserId(userId);
        subscription.setEndpoint(req.endpoint());
        subscription.setP256dh(req.p256dh());
        subscription.setAuthKey(req.authKey());

        if (subscription.getCreatedAt() == null) {
            subscription.setCreatedAt(OffsetDateTime.now());
        }

        pushSubscriptionRepository.save(subscription);
    }

    public void sendToAll(UUID excludeUserId, String title, String body) {
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findAllByUserIdNot(excludeUserId);

        AppProperties.Vapid vapid = appProperties.getVapid();

        PushService pushService;
        try {
            pushService = new PushService(vapid.getPublicKey(), vapid.getPrivateKey(), vapid.getSubject());
        } catch (Exception e) {
            log.error("Failed to initialize PushService", e);
            return;
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of("title", title, "body", body));
        } catch (Exception e) {
            log.error("Failed to serialize push payload", e);
            return;
        }

        for (PushSubscription sub : subscriptions) {
            try {
                Subscription.Keys keys = new Subscription.Keys(sub.getP256dh(), sub.getAuthKey());
                Subscription subscription = new Subscription(sub.getEndpoint(), keys);
                Notification notification = new Notification(subscription, payload);

                HttpResponse httpResponse = pushService.send(notification);
                int statusCode = httpResponse.getStatusLine().getStatusCode();

                if (statusCode == 410) {
                    pushSubscriptionRepository.delete(sub);
                }
            } catch (Exception e) {
                log.warn("Failed to send push notification to endpoint {}: {}", sub.getEndpoint(), e.getMessage());
            }
        }
    }
}
