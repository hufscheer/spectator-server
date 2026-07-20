package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.domain.TimelineDeletabilityEvaluator;

import java.util.List;
import java.util.Map;

public record TimelineResponse(
        QuarterResponse gameQuarter,
        List<RecordResponse> records
) {
    public static TimelineResponse of(Quarter quarter, List<Timeline> timelines,
                                      Map<Long, TimelineDeletabilityEvaluator.Result> deletability) {
        return new TimelineResponse(
                QuarterResponse.from(quarter),
                timelines.stream()
                        .map(timeline -> RecordResponse.from(timeline, deletability.get(timeline.getId())))
                        .toList()
        );
    }
}
