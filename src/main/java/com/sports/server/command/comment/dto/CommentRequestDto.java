package com.sports.server.command.comment.dto;

import lombok.NonNull;

public record CommentRequestDto(
        @NonNull String content,
        @NonNull Long gameTeamId
) {
}
