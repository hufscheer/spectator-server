package com.sports.server.command.team.domain;

public record PlayerGoalCount(
        Long playerId,
        Long playerTotalGoalCount
) {
}
