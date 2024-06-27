package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.Timeline;

import java.util.List;

public record TimelineResponse(
        String gameQuarter,
        List<RecordResponse> records
) {
    public static TimelineResponse of(String quarter, List<Timeline> timelines) {
        return null;
    }
}
