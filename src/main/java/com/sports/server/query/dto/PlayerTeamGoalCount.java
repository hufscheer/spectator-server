package com.sports.server.query.dto;

public record PlayerTeamGoalCount(
        Long playerId,
        Long teamId,
        Long goalCount,
        Long lastScoreTimelineId
) {
}
