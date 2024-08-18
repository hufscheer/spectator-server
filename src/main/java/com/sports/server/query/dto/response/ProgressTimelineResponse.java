package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.GameProgressType;

public record ProgressTimelineResponse(
        GameProgressType gameProgressType
) {
    public static ProgressTimelineResponse from(GameProgressTimeline timeline) {
        return new ProgressTimelineResponse(
                timeline.getGameProgressType()
        );
    }
}
