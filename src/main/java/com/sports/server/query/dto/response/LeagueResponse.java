package com.sports.server.query.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueProgress;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LeagueResponse(
        Long leagueId,
        String name,
        int maxRound,
        int inProgressRound,
        String leagueProgress,
        String winnerTeamName
) {
    public LeagueResponse(League league, String winnerTeamName) {
        this(
                league.getId(),
                league.getName(),
                league.getMaxRound().getNumber(),
                league.getInProgressRound().getNumber(),
                LeagueProgress.fromDate(LocalDateTime.now(), league).getDescription(), winnerTeamName
        );
    }
}
