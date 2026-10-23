package com.sports.server.query.dto;

public record PlayerTeamAssistCount(
        Long playerId,
        Long teamId,
        Long assistCount,
        Long lastAssistTimelineId
) {
}
