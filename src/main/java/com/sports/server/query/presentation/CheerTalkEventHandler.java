package com.sports.server.query.presentation;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkCreateEvent;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.CheerTalkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheerTalkEventHandler {

    private static final String DESTINATION = "/topic/games/";
    private final EntityUtils entityUtils;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    @Async("asyncThreadPool")
    public void handle(CheerTalkCreateEvent event) {

        CheerTalk cheerTalk = event.cheerTalk();

        try {
            GameTeam gameTeam = entityUtils.getEntity(cheerTalk.getGameTeamId(), GameTeam.class);
            Game game = gameTeam.getGame();
            String destination = DESTINATION + game.getId();

            messagingTemplate.convertAndSend(
                    destination,
                    new CheerTalkResponse.ForSpectator(cheerTalk)
            );

        } catch (MessagingException e) {
            log.error("CheerTalk WebSocket 전송 실패: cheerTalkId={}, gameTeamId={}, error={}",
                    cheerTalk.getId(), cheerTalk.getGameTeamId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("CheerTalk WebSocket 전송 중 예상치 못한 오류: cheerTalkId={}, gameTeamId={}, error={}",
                    cheerTalk.getId(), cheerTalk.getGameTeamId(), e.getMessage(), e);
        }
    }
}
