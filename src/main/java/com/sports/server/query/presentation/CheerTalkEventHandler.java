package com.sports.server.query.presentation;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkCreateEvent;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.application.GameTeamServiceUtils;
import com.sports.server.query.dto.response.CheerTalkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CheerTalkEventHandler {

    private static final String DESTINATION = "/topic/games/";

    private final GameTeamServiceUtils gameTeamServiceUtils;
    private final EntityUtils entityUtils;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    @Async("asyncThreadPool")
    public void handle(CheerTalkCreateEvent event) {

        CheerTalk cheerTalk = event.cheerTalk();
        GameTeam gameTeam = entityUtils.getEntity(cheerTalk.getGameTeamId(), GameTeam.class);
        Game game = gameTeam.getGame();

        messagingTemplate.convertAndSend(
                DESTINATION + game.getId(),
                new CheerTalkResponse(cheerTalk)
        );

    }
}
