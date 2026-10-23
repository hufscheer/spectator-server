package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.LeagueTopScorer;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.util.StudentNumber;

public record TopScorerResponse(
        Long playerId,
        String playerName,
        String admissionYear,
        Integer ranking,
        Integer goalCount,
        Long teamId,
        String teamName,
        String logoImageUrl
) {
    public static TopScorerResponse of(LeagueTopScorer leagueTopScorer, Team team) {
        String studentNumber = leagueTopScorer.getPlayer().getStudentNumber();
        String admissionYear = StudentNumber.extractAdmissionYear(studentNumber);

        return new TopScorerResponse(
                leagueTopScorer.getPlayer().getId(),
                leagueTopScorer.getPlayer().getName(),
                admissionYear,
                leagueTopScorer.getRanking(),
                leagueTopScorer.getGoalCount(),
                team == null ? null : team.getId(),
                team == null ? null : team.getName(),
                team == null ? null : team.getLogoImageUrl()
        );
    }

    public static TopScorerResponse of(Player player, Integer totalGoals) {
        String studentNumber = player.getStudentNumber();
        String admissionYear = StudentNumber.extractAdmissionYear(studentNumber);

        return new TopScorerResponse(
                player.getId(),
                player.getName(),
                admissionYear,
                null,
                totalGoals,
                null,
                null,
                null
        );
    }

    public static TopScorerResponse of(Long playerId, String studentNumber, String playerName, Integer totalGoals, Integer ranking) {
        String admissionYear = StudentNumber.extractAdmissionYear(studentNumber);

        return new TopScorerResponse(
                playerId,
                playerName,
                admissionYear,
                ranking,
                totalGoals,
                null,
                null,
                null
        );
    }

}