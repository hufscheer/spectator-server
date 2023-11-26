package com.sports.server.comment.dto.response;

import com.sports.server.comment.domain.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String content,
        Long gameTeamId,
        LocalDateTime createdAt,
        Boolean isBlocked
) {
    public CommentResponse(Comment comment) {
        this(
                comment.getId(),
                comment.getContent(),
                comment.getGameTeamId(),
                comment.getCreatedAt(),
                comment.isBlocked()
        );
    }
}
