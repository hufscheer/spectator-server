package com.sports.server.command.team.domain;

public record PlayerGoalCountWithRank(
        Long playerId,
        String studentNumber,
        String playerName,
        Long goalCount,
        Long rank
) {
}
