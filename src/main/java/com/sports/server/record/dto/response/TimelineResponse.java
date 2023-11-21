package com.sports.server.record.dto.response;

import java.util.List;

public record TimelineResponse(
        String gameQuarter,
        List<RecordResponse> records
) {

    public record RecordResponse(
            Integer scoredAt,
            String playerName,
            String teamName,
            Integer score
    ) {
    }
}
