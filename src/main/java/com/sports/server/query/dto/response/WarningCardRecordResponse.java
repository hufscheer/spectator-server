package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.WarningCardTimeline;
import com.sports.server.command.timeline.domain.WarningCardType;

public record WarningCardRecordResponse (
        WarningCardType warningCardType
){
    public static WarningCardRecordResponse from(WarningCardTimeline timeline){
        return new WarningCardRecordResponse(
                timeline.getWarningCardType()
        );
    }
}
