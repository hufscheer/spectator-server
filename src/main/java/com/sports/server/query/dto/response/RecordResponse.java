package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sports.server.command.sport.domain.Quarter;

public record RecordResponse(
        @JsonIgnore
        Quarter quarter,
        String type,
        Integer recordedAt,
        String playerName,
        String teamName,
        String teamImageUrl,
        ScoreRecordResponse scoreRecord,
        ReplacementRecordResponse replacementRecord
) {
}
