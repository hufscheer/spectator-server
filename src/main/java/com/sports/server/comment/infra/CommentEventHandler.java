package com.sports.server.comment.infra;

import com.sports.server.comment.domain.Comment;
import com.sports.server.comment.domain.CommentEvent;
import com.sports.server.comment.dto.response.CommentResponseDto;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.game.domain.GameTeam;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CommentEventHandler {

    private final EntityUtils entityUtils;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    @Async("asyncThreadPool")
    public void handle(CommentEvent event) {

        Comment comment = event.comment();
        GameTeam gameTeam = entityUtils.getEntity(comment.getGameTeamId(), GameTeam.class);
        Long gameId = gameTeam.getGame().getId();

        messagingTemplate.convertAndSend(
                "/topic/games/" + gameId,
                new CommentResponseDto(comment)
        );

    }

}