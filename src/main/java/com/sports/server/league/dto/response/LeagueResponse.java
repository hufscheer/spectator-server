package com.sports.server.league.dto.response;

import com.sports.server.league.domain.League;

public record LeagueResponse(String name) {
    public LeagueResponse(League league) {
        this(league.getName());
    }
}
