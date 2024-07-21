package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.League;
import com.sports.server.query.application.InProgressLeagueChecker;

import java.time.LocalDate;

public record LeagueResponse(
        Long leagueId,
        String name,
        String maxRound,
        String inProgressRound,
        Boolean isInProgress
) {
    public LeagueResponse(League league) {
        this(
                league.getId(),
                league.getName(),
                league.getMaxRound().getDescription(),
                league.getInProgressRound().getDescription(),
                InProgressLeagueChecker.check(LocalDate.now(), league)
        );
    }
}
