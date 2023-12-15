package com.sports.server.command.comment.infra;

import com.sports.server.command.comment.domain.Comment;
import com.sports.server.command.comment.domain.CommentEvent;
import com.sports.server.command.comment.dto.response.CommentResponse;
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

        Comment comment = event.comment();
        GameTeam gameTeam = entityUtils.getEntity(comment.getGameTeamId(), GameTeam.class);
        Game game = gameTeam.getGame();

        messagingTemplate.convertAndSend(
                DESTINATION + game.getId(),
                new CommentResponse(comment,
                        gameTeamServiceUtils.calculateOrderOfGameTeam(game, comment.getGameTeamId()))
        );

    }
}
