package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import com.sports.server.command.team.domain.Team;
import com.sports.server.query.application.TeamStatistics;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
                              final List<TeamTopScorer> topScorers, final List<Trophy> trophies) {
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

    public TeamDetailResponse(final Team team, final TeamStatistics teamStats, final List<PlayerResponse> teamPlayers) {
        this(
                team,
                teamPlayers,
                teamStats.gameResultsMap().get(team.getId()),
                teamStats.topScorersMap().get(team.getId()),
                teamStats.trophiesMap().get(team.getId())
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
    ) {

        private static final int ADMISSION_YEAR_START_INDEX = 2;
        private static final int ADMISSION_YEAR_END_INDEX = 4;

        public TeamTopScorer(final PlayerGoalCountWithRank player) {
            this(
                    player.playerId(),
                    extractAdmissionYear(player.studentNumber()),
                    player.rank().intValue(),
                    player.playerName(),
                    player.goalCount().intValue()
            );
        }

        private static String extractAdmissionYear(String studentNumber) {
            if (studentNumber == null || studentNumber.length() < ADMISSION_YEAR_END_INDEX) {
                return null;
            }
            return studentNumber.substring(ADMISSION_YEAR_START_INDEX, ADMISSION_YEAR_END_INDEX);
        }
    }

    public record Trophy(
            Long leagueId,
            String leagueName,
            String trophyType
    ) {
    }
}
