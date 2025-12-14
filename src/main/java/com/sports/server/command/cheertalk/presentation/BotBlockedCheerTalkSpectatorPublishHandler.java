package com.sports.server.command.cheertalk.presentation;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkBlockedByBotEvent;
import com.sports.server.command.cheertalk.domain.PendingCheerTalk;
import com.sports.server.command.cheertalk.domain.PendingCheerTalkRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.CheerTalkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotBlockedCheerTalkSpectatorPublishHandler {

    private static final String DESTINATION = "/topic/games/";
    private final SimpMessagingTemplate messagingTemplate;
    private final EntityUtils entityUtils;

    @TransactionalEventListener
    @Async("asyncThreadPool")
    public void handle(CheerTalkBlockedByBotEvent event) {
        Long cheerTalkId = event.cheerTalkId();
        String destination = DESTINATION + event.gameId();

        CheerTalk cheerTalk = entityUtils.getEntity(cheerTalkId, CheerTalk.class);

        try {
            messagingTemplate.convertAndSend(
                    destination,
                    new CheerTalkResponse.ForSpectator(cheerTalk)
            );

            log.info(
                    "AI 차단 응원톡 소켓 메시지 발행: cheerTalkId={}, gameId={}",
                    cheerTalkId,
                    event.gameId()
            );
        } catch (Exception e) {
            /**
             * 추후 '전송 실패 응원톡 저장 로직 논의' 필요
             */

            log.error(
                    "AI 차단 응원톡 소켓 메시지 발행 실패: cheerTalkId={}, gameId={}, error={}",
                    cheerTalkId,
                    event.gameId(),
                    e.getMessage(),
                    e
            );
        }
    }
}
