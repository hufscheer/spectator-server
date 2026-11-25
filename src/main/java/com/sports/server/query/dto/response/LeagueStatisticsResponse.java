package com.sports.server.query.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record LeagueStatisticsResponse(
        LeagueTeamResponse firstWinnerTeam,
        LeagueTeamResponse secondWinnerTeam,
        LeagueTeamResponse mostCheeredTeam,
        LeagueTeamResponse mostCheerTalksTeam,
        List<LeagueTeamRankingResponse> cheerRanking,
        List<LeagueTeamRankingResponse> cheerTalksRanking
) {
}
