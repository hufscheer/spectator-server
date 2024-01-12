package com.sports.server.command.cheertalk.dto;

import lombok.NonNull;

public record CheerTalkRequest(
        @NonNull String content,
        @NonNull Long gameTeamId
) {
}
