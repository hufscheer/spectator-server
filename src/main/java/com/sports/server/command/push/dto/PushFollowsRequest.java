package com.sports.server.command.push.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PushFollowsRequest(
        @NotNull List<@NotNull Long> teamIds,
        @NotNull List<@NotNull Long> gameIds
) {
}
