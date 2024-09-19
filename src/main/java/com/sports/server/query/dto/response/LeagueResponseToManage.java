package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueProgress;

import java.time.LocalDateTime;
import java.util.List;

public record LeagueResponseToManage(
        Long id,
        String name,
        String leagueProgress,
        int sizeOfLeagueTeams,
        String maxRound,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
    public LeagueResponseToManage(League league) {
        this(
            league.getId(),
            league.getName(),
            LeagueProgress.getProgressDescription(LocalDateTime.now(), league),
            league.getLeagueTeams().size(),
            league.getMaxRound().getDescription(),
            league.getStartAt(),
            league.getEndAt()
        );
    }
}
