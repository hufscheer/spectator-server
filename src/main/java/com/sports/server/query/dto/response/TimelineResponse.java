package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.Timeline;

import java.util.List;

public record TimelineResponse(
        String gameQuarter,
        String gameQuarterDisplayName,
        List<RecordResponse> records
) {
    public static TimelineResponse of(String quarter, String displayName, List<Timeline> timelines) {
        return new TimelineResponse(
                quarter,
                displayName,
                timelines.stream()
                        .map(RecordResponse::from)
                        .toList()
        );
    }
}
