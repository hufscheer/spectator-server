package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.TimelineChangedEvent;

public record TimelineChangedResponse(
        Long gameId,
        String changeType
) {
    public static TimelineChangedResponse from(TimelineChangedEvent event) {
        return new TimelineChangedResponse(event.gameId(), event.changeType().name());
    }
}
