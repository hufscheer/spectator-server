package com.sports.server.query.dto.response;

import java.util.List;

public record ScoreRecordResponse(
        Integer point,
        List<Snapshot> snapshot
) {

    public record Snapshot(
            String teamName,
            String teamImageUrl,
            Integer score
    ) {
    }
}
