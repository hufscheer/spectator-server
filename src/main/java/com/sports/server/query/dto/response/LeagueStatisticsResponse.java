package com.sports.server.query.dto.response;

import lombok.Builder;

@Builder
public record LeagueStatisticsResponse(
        Long leagueStatisticsId,
        TeamResponse firstWinnerTeam,
        TeamResponse secondWinnerTeam,
        TeamResponse mostCheeredTeam,
        TeamResponse mostCheerTalksTeam
) {
}
