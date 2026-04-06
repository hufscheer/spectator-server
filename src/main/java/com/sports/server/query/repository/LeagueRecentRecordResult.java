package com.sports.server.query.repository;

import com.sports.server.query.dto.response.LeagueRecentSummaryResponse;

public record LeagueRecentRecordResult(
        Long leagueId,
        String name,
        String winnerTeamName,
        String sportType
) {
    public LeagueRecentSummaryResponse.LeagueRecord toResponse() {
        return new LeagueRecentSummaryResponse.LeagueRecord(leagueId, name, winnerTeamName, sportType);
    }
}
