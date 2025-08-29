package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.League;

import java.util.List;

public record LeagueWithGamesResponse(
        Long leagueId,
        String leagueName,
        List<GameResponseDto> games
) {
    public LeagueWithGamesResponse(League league, List<Game> games) {
        this(
                league.getId(),
                league.getName(),
                games.stream()
                        .map(game -> new GameResponseDto(game, game.getGameTeams()))
                        .toList()
        );
    }
}
