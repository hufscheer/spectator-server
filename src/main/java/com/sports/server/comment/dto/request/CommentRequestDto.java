package com.sports.server.comment.dto.request;

public record CommentRequestDto(
        String content,
        Long gameTeamId
) {
}
