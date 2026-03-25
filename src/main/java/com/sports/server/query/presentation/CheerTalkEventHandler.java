package com.sports.server.query.presentation;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkCreateEvent;
import com.sports.server.command.cheertalk.domain.CheerTalkMaskingEvent;
import com.sports.server.query.dto.response.CheerTalkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheerTalkEventHandler {

    private static final String DESTINATION = "/topic/games/";
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener
    @Async("asyncThreadPool")
    public void handle(CheerTalkCreateEvent event) {
        CheerTalk cheerTalk = event.cheerTalk();
        String destination = DESTINATION + event.gameId();

        try {
            messagingTemplate.convertAndSend(
                    destination,
                    new CheerTalkResponse.ForSpectator(cheerTalk)
            );

            eventPublisher.publishEvent(new CheerTalkMaskingEvent(cheerTalk, event.gameId()));

        } catch (Exception e) {

            log.error(
                    "CheerTalk WebSocket 전송 실패: cheerTalkId={}, gameTeamId={}, error={}",
                    cheerTalk.getId(), cheerTalk.getGameTeamId(), e.getMessage(), e
            );
        }
    }
}
