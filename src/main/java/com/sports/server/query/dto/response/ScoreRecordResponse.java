package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.ScoreTimeline;

import java.util.List;

public record ScoreRecordResponse(
        Long scoreRecordId,
        Integer score,
        List<Snapshot> snapshot
) {

    public static ScoreRecordResponse from(ScoreTimeline scoreTimeline) {
        return new ScoreRecordResponse(
                scoreTimeline.getId(),
                scoreTimeline.getScore(),
                List.of()
        );
    }

    public record Snapshot(
            String teamName,
            String teamImageUrl,
            Integer score
    ) {
    }
}
