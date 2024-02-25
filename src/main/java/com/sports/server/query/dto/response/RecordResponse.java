package com.sports.server.query.dto.response;

public record RecordResponse(
        String type,
        Integer recordedAt,
        String playerName,
        String teamName,
        String teamImageUrl,
        Integer order,
        ScoreRecordResponse score,
        ReplacementRecordResponse replacement
) {
}
