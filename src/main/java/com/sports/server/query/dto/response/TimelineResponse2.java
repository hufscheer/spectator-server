package com.sports.server.query.dto.response;

import java.util.List;

public record TimelineResponse2(
        String gameQuarter,
        List<RecordResponse> records
) {
}
