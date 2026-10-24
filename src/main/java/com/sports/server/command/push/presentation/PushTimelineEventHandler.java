package com.sports.server.command.push.presentation;

import com.sports.server.command.push.application.PushNotificationService;
import com.sports.server.command.timeline.domain.TimelineCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushTimelineEventHandler {

    private final PushNotificationService pushNotificationService;

    @TransactionalEventListener
    @Async("pushThreadPool")
    public void handle(TimelineCreatedEvent event) {
        try {
            pushNotificationService.notifyTimelineCreated(event);
        } catch (Exception e) {
            log.error("푸시 알림 처리 실패: timelineId={}, gameId={}, error={}",
                    event.timelineId(), event.gameId(), e.getMessage(), e);
        }
    }
}
