package com.sports.server.command.push.application;

import com.sports.server.command.push.domain.PushBroadcast;
import com.sports.server.command.timeline.domain.TimelineCreatedEvent;
import com.sports.server.command.timeline.domain.TimelineType;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private static final Set<TimelineType> NOTIFIABLE_TYPES =
            Set.of(TimelineType.GAME_PROGRESS, TimelineType.SCORE, TimelineType.OWN_GOAL);

    private final PushBroadcastFactory broadcastFactory;
    private final PushSender pushSender;
    private final PushDeviceService pushDeviceService;

    public void notifyTimelineCreated(TimelineCreatedEvent event) {
        if (!NOTIFIABLE_TYPES.contains(event.type())) {
            return;
        }
        broadcastFactory.create(event.timelineId())
                .filter(PushBroadcast::hasRecipients)
                .ifPresent(this::deliver);
    }

    private void deliver(PushBroadcast broadcast) {
        List<Long> invalidDeviceIds = pushSender.send(broadcast.message(), broadcast.recipients());
        if (!invalidDeviceIds.isEmpty()) {
            pushDeviceService.removeAll(invalidDeviceIds);
        }
    }
}
