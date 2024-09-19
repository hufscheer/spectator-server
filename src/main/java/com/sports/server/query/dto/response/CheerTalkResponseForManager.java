package com.sports.server.query.dto.response;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.game.domain.Game;
import java.time.LocalDateTime;

public record CheerTalkResponseForManager(
        Long cheerTalkId,
        Long gameId,
        Long leagueId,
        String content,
        Long gameTeamId,
        LocalDateTime createdAt,
        Boolean isBlocked,
        String gameName,
        String leagueName
) {
    public CheerTalkResponseForManager(CheerTalk cheerTalk, Game game) {
        this(
                cheerTalk.getId(),
                game.getId(),
                game.getLeague().getId(),
                cheerTalk.getContent(),
                cheerTalk.getGameTeamId(),
                cheerTalk.getCreatedAt(),
                cheerTalk.isBlocked(),
                game.getName(),
                game.getLeague().getName()
        );
    }
}
