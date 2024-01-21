package com.sports.server.query.dto.response;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import java.time.LocalDateTime;

public record CheerTalkResponse(
        Long cheerTalkId,
        String content,
        Long gameTeamId,
        LocalDateTime createdAt,
        Boolean isBlocked,
        int order
) {
    public CheerTalkResponse(CheerTalk cheerTalk, final int order) {
        this(
                cheerTalk.getId(),
                checkCheerTalkIsBlocked(cheerTalk),
                cheerTalk.getGameTeamId(),
                cheerTalk.getCreatedAt(),
                cheerTalk.isBlocked(),
                order
        );
    }

    private static String checkCheerTalkIsBlocked(CheerTalk cheerTalk) {
        if (cheerTalk.isBlocked()) {
            return null;
        }
        return cheerTalk.getContent();
    }
}
