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
        int maxRound,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
    public static LeagueResponseToManage of(League league) {
        return new LeagueResponseToManage(
            league.getId(),
            league.getName(),
            LeagueProgress.getProgressDescription(LocalDateTime.now(), league),
            league.getLeagueTeams().size(),
            league.getMaxRound().getNumber(),
            league.getStartAt(),
            league.getEndAt()
        );
    }
}
