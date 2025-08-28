package com.sports.server.query.dto.response;

import com.sports.server.command.team.domain.Team;

import java.util.List;

public record TeamDetailResponse(
        String name,
        String logoImageUrl,
        String unit,
        String teamColor,
        List<PlayerResponse> teamPlayers,
        int winCount, int drawCount, int loseCount,
        List<TeamTopScorer> topScorers,
        List<Trophy> trophies
) {
    public TeamDetailResponse(final Team team,  final List<PlayerResponse> teamPlayers,
                              final TeamDetailResponse.TeamGameResult teamGameResult,
                              final List<TeamTopScorer> topScorers, final List<Trophy> trophies){
        this(
                team.getName(),
                team.getLogoImageUrl(),
                team.getUnit().getName(),
                team.getTeamColor(),
                teamPlayers,
                teamGameResult.winCount(),  teamGameResult.drawCount(),  teamGameResult.loseCount(),
                topScorers,
                trophies
        );
    }

    public record TeamGameResult(
            int winCount, int drawCount, int loseCount
    ){
    }

    public record TeamTopScorer(
            Long playerId,
            String admissionYear,
            int rank,
            String playerName,
            int totalGoals
    ){
    }

    public record Trophy(
            Long leagueId,
            String leagueName,
            String trophyType
    ) {
    }

}
