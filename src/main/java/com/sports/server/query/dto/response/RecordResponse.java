package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sports.server.command.sport.domain.Quarter;

public record RecordResponse(
        @JsonIgnore
        Quarter quarter,
        Long recordId,
        String type,
        Integer recordedAt,
        String playerName,
        Long gameTeamId,
        String teamName,
        String teamImageUrl,
        ScoreRecordResponse scoreRecord,
        ReplacementRecordResponse replacementRecord
) {
}
