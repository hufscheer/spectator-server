package com.sports.server.query.presentation;

import com.sports.server.command.timeline.domain.TimelineChangedEvent;
import com.sports.server.query.dto.response.TimelineChangedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 응원톡이 쓰는 {@code /topic/games/{gameId}} 와 목적지를 나눈다. 같은 목적지에 형태가 다른 메시지를
 * 섞으면 이미 붙어 있는 응원톡 구독이 깨질 수 있다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimelineChangeEventHandler {

    private static final String DESTINATION_FORMAT = "/topic/games/%d/timeline";

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    @Async("asyncThreadPool")
    public void handle(TimelineChangedEvent event) {
        try {
            messagingTemplate.convertAndSend(
                    String.format(DESTINATION_FORMAT, event.gameId()),
                    TimelineChangedResponse.from(event)
            );
        } catch (Exception e) {
            log.error("타임라인 변경 WebSocket 전송 실패: gameId={}, changeType={}, error={}",
                    event.gameId(), event.changeType(), e.getMessage(), e);
        }
    }
}
