package com.sports.server.command.comment.dto.request;

import lombok.NonNull;

public record CommentRequestDto(
        @NonNull String content,
        @NonNull Long gameTeamId
) {
}
