package com.sports.server.command.timeline.domain;

public record TimelineCreatedEvent(
        Long timelineId,
        Long gameId,
        TimelineType type
) {
}