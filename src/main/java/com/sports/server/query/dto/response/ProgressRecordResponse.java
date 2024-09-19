package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.GameProgressType;

public record ProgressRecordResponse(
        GameProgressType gameProgressType
) {
    public static ProgressRecordResponse from(GameProgressTimeline timeline) {
        return new ProgressRecordResponse(
                timeline.getGameProgressType()
        );
    }
}
