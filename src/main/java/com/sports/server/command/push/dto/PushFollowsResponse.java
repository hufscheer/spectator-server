package com.sports.server.command.push.dto;

import java.util.List;

public record PushFollowsResponse(
        List<Long> teamIds,
        List<Long> gameIds
) {
}
