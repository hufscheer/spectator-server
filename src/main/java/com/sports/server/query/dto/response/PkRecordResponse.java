package com.sports.server.query.dto.response;

import com.sports.server.command.timeline.domain.PKTimeline;

public record PkRecordResponse(
        Long pkRecordId,
        boolean isSuccess
) {
    public static PkRecordResponse from(PKTimeline pkTimeline) {
        return new PkRecordResponse(
                pkTimeline.getId(), pkTimeline.getIsSuccess()
        );
    }
}
