package com.sports.server.query.dto.response;

import lombok.Builder;

@Builder
public record LeagueStatisticsResponse(
        LeagueTeamResponse firstWinnerTeam,
        LeagueTeamResponse secondWinnerTeam,
        LeagueTeamResponse mostCheeredTeam,
        LeagueTeamResponse mostCheerTalksTeam
) {
}
