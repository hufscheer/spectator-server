package com.sports.server.query.presentation;

import com.sports.server.command.comment.domain.CheerTalk;
import com.sports.server.command.comment.domain.CommentEvent;
import com.sports.server.query.dto.response.CommentResponse;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.application.GameTeamServiceUtils;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CommentEventHandler {

    private static final String DESTINATION = "/topic/games/";

    private final GameTeamServiceUtils gameTeamServiceUtils;
    private final EntityUtils entityUtils;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    @Async("asyncThreadPool")
    public void handle(CommentEvent event) {

        CheerTalk cheerTalk = event.cheerTalk();
        GameTeam gameTeam = entityUtils.getEntity(cheerTalk.getGameTeamId(), GameTeam.class);
        Game game = gameTeam.getGame();

        messagingTemplate.convertAndSend(
                DESTINATION + game.getId(),
                new CommentResponse(cheerTalk,
                        gameTeamServiceUtils.calculateOrderOfGameTeam(game, cheerTalk.getGameTeamId()))
        );

    }
}
