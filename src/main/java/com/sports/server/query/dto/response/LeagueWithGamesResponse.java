package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.League;

import java.util.List;

public record LeagueWithGamesResponse(
        Long leagueId,
        String leagueName,
        List<GameResponseDto> games
) {
    public LeagueWithGamesResponse(League league, List<GameResponseDto> games) {
        this(
                league.getId(),
                league.getName(),
                games
        );
    }
}
