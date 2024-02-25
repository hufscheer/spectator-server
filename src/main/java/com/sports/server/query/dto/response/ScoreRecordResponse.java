package com.sports.server.query.dto.response;

import java.util.List;

public record ScoreRecordResponse(
        Integer point,
        List<History> histories
) {

    public record History(
            String teamName,
            String teamImageUrl,
            Integer score
    ) {
    }
}
