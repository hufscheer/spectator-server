package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.LeagueStatistics;

public record LeagueStatisticsResponse(
        Long leagueStatisticsId,
        TeamResponse firstWinnerTeam,
        TeamResponse secondWinnerTeam,
        TeamResponse mostCheeredTeam,
        TeamResponse mostCheerTalksTeam
) {
    public LeagueStatisticsResponse(LeagueStatistics leagueStatistics){
        this(
                leagueStatistics.getId(),
                new TeamResponse(leagueStatistics.getFirstWinnerTeam()),
                new TeamResponse(leagueStatistics.getSecondWinnerTeam()),
                new TeamResponse(leagueStatistics.getMostCheeredTeam()),
                new TeamResponse(leagueStatistics.getMostCheerTalksTeam())
        );
    }
}
