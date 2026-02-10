package com.sports.server.query.dto.response;

import java.util.List;

public record LeagueRecentSummaryResponse(
        List<LeagueRecord> records,
        List<TopScorer> topScorers
) {
    public record LeagueRecord(
            Long leagueId,
            String name,
            String winnerTeamName
    ) {
        public static LeagueRecord from(LeagueResponse leagueResponse) {
            return new LeagueRecord(
                    leagueResponse.leagueId(),
                    leagueResponse.name(),
                    leagueResponse.winnerTeamName()
            );
        }
    }

    public record TopScorer(
            Long playerId,
            String admissionYear,
            int rank,
            String playerName,
            int totalGoals
    ) {
    }
}
