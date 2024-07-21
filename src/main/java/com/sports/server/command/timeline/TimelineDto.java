package com.sports.server.command.timeline;

public class TimelineDto {
    public record RegisterScore(
            Long gameTeamId,
            Long recordedQuarterId,
            Long scoreLineupPlayerId,
            Integer recordedAt
    ) {
    }
}
