package io.ahnlab.familytown.dto;

public record PushSubscribeRequest(
        String endpoint,
        String p256dh,
        String authKey
) {
}
