package com.sports.server.query.dto;

public record GameTeamGameInfoDto(
        Long gameTeamId,
        Long gameId,
        String gameName,
        Long leagueId,
        String leagueName
) {
}
