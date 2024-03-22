package com.sports.server.query.dto.response;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record CheerTalkResponse(
        Long cheerTalkId,
        String content,
        Long gameTeamId,
        LocalDateTime createdAt,
        Boolean isBlocked
) {
    public CheerTalkResponse(CheerTalk cheerTalk) {
        this(
                cheerTalk.getId(),
                checkCheerTalkIsBlocked(cheerTalk),
                cheerTalk.getGameTeamId(),
                cheerTalk.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
                cheerTalk.isBlocked()
        );
    }

    private static String checkCheerTalkIsBlocked(CheerTalk cheerTalk) {
        if (cheerTalk.isBlocked()) {
            return null;
        }
        return cheerTalk.getContent();
    }
}
