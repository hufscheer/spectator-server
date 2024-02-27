package com.sports.server.query.dto.response;

import java.util.List;

public record TimelineResponse(
        String gameQuarter,
        List<RecordResponse> records
) {
}
