package com.sports.server.command.comment.dto;

import lombok.NonNull;

public record CheerTalkRequest(
        @NonNull String content,
        @NonNull Long gameTeamId
) {
}
