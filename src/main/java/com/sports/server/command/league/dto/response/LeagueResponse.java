package com.sports.server.command.league.dto.response;

import com.sports.server.command.league.domain.League;

public record LeagueResponse(
        Long leagueId,
        String name
) {
    public LeagueResponse(League league) {
        this(league.getId(), league.getName());
    }
}
