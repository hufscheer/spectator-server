package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.GameProgressType;

public record ProgressRecordResponse(
        GameProgressType gameProgressType
) {
}
