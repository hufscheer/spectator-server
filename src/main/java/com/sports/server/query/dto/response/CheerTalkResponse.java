package com.sports.server.query.dto.response;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import java.time.LocalDateTime;

public class CheerTalkResponse {
    public record ForSpectator(
            Long cheerTalkId,
            String content,
            Long gameTeamId,
            LocalDateTime createdAt,
            Boolean isBlocked
    ) {
        public ForSpectator(CheerTalk cheerTalk) {
            this(
                    cheerTalk.getId(),
                    checkCheerTalkIsBlocked(cheerTalk),
                    cheerTalk.getGameTeamId(),
                    cheerTalk.getCreatedAt(),
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

}

