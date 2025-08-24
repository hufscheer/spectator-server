package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.LeagueTopScorer;

public record LeagueTopScorerResponse(
        Long playerId,
        String playerName,
        String studentNumber,
        Integer ranking,
        Integer goalCount
) {
    private static final int ADMISSION_YEAR_START_INDEX = 2;
    private static final int ADMISSION_YEAR_END_INDEX = 4;

    public static LeagueTopScorerResponse from(LeagueTopScorer leagueTopScorer) {
        String studentNumber = leagueTopScorer.getPlayer().getStudentNumber();
        String admissionYear = extractAdmissionYear(studentNumber);

        return new LeagueTopScorerResponse(
                leagueTopScorer.getPlayer().getId(),
                leagueTopScorer.getPlayer().getName(),
                admissionYear,
                leagueTopScorer.getRanking(),
                leagueTopScorer.getGoalCount()
        );
    }

    private static String extractAdmissionYear(String studentNumber) {
        if (studentNumber == null || studentNumber.length() < ADMISSION_YEAR_END_INDEX) {
            return null;
        }
        return studentNumber.substring(ADMISSION_YEAR_START_INDEX, ADMISSION_YEAR_END_INDEX);
    }
}