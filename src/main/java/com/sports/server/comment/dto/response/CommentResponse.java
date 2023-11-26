package com.sports.server.comment.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String content,
        Long gameTeamId,
        LocalDateTime createdAt,
        Boolean isBlocked
) {
}
