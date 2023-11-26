package com.sports.server.comment.dto.response;

import com.sports.server.comment.domain.Comment;
import java.time.LocalDateTime;

public record CommentResponseDto(
        Long id,
        String content,
        Long gameTeamId,
        boolean isBlocked,
        LocalDateTime createdAt
) {
    public CommentResponseDto(final Comment comment) {
        this(
                comment.getId(), comment.getContent(), comment.getGameTeamId(), comment.isBlocked(),
                comment.getCreatedAt()
        );
    }
}