package com.sports.server.command.push.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PushFollowsRequest(
        @NotNull List<@NotNull Long> teamIds,
        @NotNull List<@NotNull Long> gameIds,
        // 이전 버전 앱은 보내지 않으므로 비어 있으면 학교 팔로우가 없는 것으로 본다
        List<@NotNull Long> organizationIds
) {
    public PushFollowsRequest(List<Long> teamIds, List<Long> gameIds) {
        this(teamIds, gameIds, List.of());
    }

    public List<Long> organizationIds() {
        return organizationIds == null ? List.of() : organizationIds;
    }
}
