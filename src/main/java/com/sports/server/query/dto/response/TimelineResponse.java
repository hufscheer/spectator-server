package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.Quarter;
import com.sports.server.command.timeline.domain.Timeline;

import java.util.List;

public record TimelineResponse(
        QuarterResponse gameQuarter,
        List<RecordResponse> records
) {
    public static TimelineResponse of(Quarter quarter, List<Timeline> timelines) {
        return new TimelineResponse(
                QuarterResponse.from(quarter),
                timelines.stream()
                        .map(RecordResponse::from)
                        .toList()
        );
    }
}
