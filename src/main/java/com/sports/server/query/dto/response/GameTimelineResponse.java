package com.sports.server.query.dto.response;

import java.util.List;

public record GameTimelineResponse(
        WinnerResponse winner,
        List<TimelineResponse> timelines
) {
}
