package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.LeagueTopScorer;

public record LeagueTopScorerResponse(
        Long playerId,
        String playerName,
        String studentNumber,
        Integer ranking,
        Integer goalCount
) {
    public static LeagueTopScorerResponse from(LeagueTopScorer leagueTopScorer) {
        return new LeagueTopScorerResponse(
                leagueTopScorer.getPlayer().getId(),
                leagueTopScorer.getPlayer().getName(),
                leagueTopScorer.getPlayer().getStudentNumber(),
                leagueTopScorer.getRanking(),
                leagueTopScorer.getGoalCount()
        );
    }
}