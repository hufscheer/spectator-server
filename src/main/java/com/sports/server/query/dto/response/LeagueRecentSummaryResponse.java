package com.sports.server.query.dto.response;

import java.util.List;

public record LeagueRecentSummaryResponse(
        List<LeagueRecord> records,
        List<TopScorerResponse> topScorers
) {
    public record LeagueRecord(
            Long leagueId,
            String leagueName,
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
}
