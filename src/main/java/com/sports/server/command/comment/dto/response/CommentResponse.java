package com.sports.server.command.comment.dto.response;

import com.sports.server.command.comment.domain.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String content,
        Long gameTeamId,
        LocalDateTime createdAt,
        Boolean isBlocked,
        int order
) {
    public CommentResponse(Comment comment, final int order) {
        this(
                comment.getId(),
                checkCommentBlocked(comment),
                comment.getGameTeamId(),
                comment.getCreatedAt(),
                comment.isBlocked(),
                order
        );
    }

    private static String checkCommentBlocked(Comment comment) {
        if (comment.isBlocked()) {
            return null;
        }
        return comment.getContent();
    }
}
