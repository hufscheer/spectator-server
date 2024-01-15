package com.sports.server.query.dto.response;

import com.sports.server.command.cheertalk.domain.CheerTalk;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String content,
        Long gameTeamId,
        LocalDateTime createdAt,
        Boolean isBlocked,
        int order
) {
    public CommentResponse(CheerTalk cheerTalk, final int order) {
        this(
                cheerTalk.getId(),
                checkCommentBlocked(cheerTalk),
                cheerTalk.getGameTeamId(),
                cheerTalk.getCreatedAt(),
                cheerTalk.isBlocked(),
                order
        );
    }

    private static String checkCommentBlocked(CheerTalk cheerTalk) {
        if (cheerTalk.isBlocked()) {
            return null;
        }
        return cheerTalk.getContent();
    }
}
