package com.sports.server.comment.dto.request;

import lombok.NonNull;

public record CommentRequestDto(
        @NonNull String content,
        @NonNull Long gameTeamId
) {
}
