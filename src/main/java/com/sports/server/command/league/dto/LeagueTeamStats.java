package com.sports.server.command.league.dto;

public record LeagueTeamStats(
    Long leagueTeamId,
    Long totalCheerCount,
    Long totalTalkCount
) {
}