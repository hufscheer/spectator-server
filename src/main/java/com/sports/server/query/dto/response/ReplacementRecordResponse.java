package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.ReplacementTimeline;

public record ReplacementRecordResponse(
        Long replacementRecordId,
        String replacedPlayerName
) {
    public static ReplacementRecordResponse from(ReplacementTimeline timeline) {
        return new ReplacementRecordResponse(
                timeline.getId(),
                timeline.getReplacedLineupPlayer().getName()
        );
    }
}
