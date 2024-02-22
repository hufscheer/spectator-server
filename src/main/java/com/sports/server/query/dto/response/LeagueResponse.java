package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.League;

public record LeagueResponse(
        Long leagueId,
        String name,
        Integer maxRound,
        Integer inProgressRound
) {
    public LeagueResponse(League league) {
        this(league.getId(), league.getName(), league.getMaxRound(), league.getInProgressRound());
    }
}
