package io.ahnlab.familytown.service;

import io.ahnlab.familytown.entity.LeaveWorkEvent;
import io.ahnlab.familytown.entity.Profile;
import io.ahnlab.familytown.repository.LeaveWorkEventRepository;
import io.ahnlab.familytown.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveWorkService {

    private final LeaveWorkEventRepository leaveWorkEventRepository;
    private final ProfileRepository profileRepository;
    private final WebPushService webPushService;

    @Transactional
    public void recordAndNotify(UUID actorId) {
        LeaveWorkEvent event = new LeaveWorkEvent();
        event.setActorId(actorId);
        event.setCreatedAt(OffsetDateTime.now());
        leaveWorkEventRepository.save(event);

        Profile actor = profileRepository.findById(actorId).orElse(null);
        String nickname = actor != null ? actor.getNickname() : "누군가";

        webPushService.sendToAll(actorId, "퇴근 알림", nickname + "님이 퇴근했어요!");
    }
}
