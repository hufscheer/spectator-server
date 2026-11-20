package com.sports.server.command.league.domain;

public record LeagueTeamTally(
        Long leagueTeamId,
        Long teamId,
        String teamName,
        String logoImageUrl,
        int cheerCount,
        int talkCount
) {
}
