package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.WarningCardType;

public record WarningCardRecordResponse (
        WarningCardType warningCardType
){
}
