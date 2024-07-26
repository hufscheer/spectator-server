package com.sports.server.command.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimelineRequest {
    private final Long gameTeamId;
    private final Long recordedQuarterId;
    private final Integer recordedAt;

    @Getter
    public static class RegisterScore extends TimelineRequest {
        private final Long scoreLineupPlayerId;

        public RegisterScore(
                Long gameTeamId,
                Long recordedQuarterId,
                Long scoreLineupPlayerId,
                Integer recordedAt
        ) {
            super(gameTeamId, recordedQuarterId, recordedAt);
            this.scoreLineupPlayerId = scoreLineupPlayerId;
        }
    }

    @Getter
    public static class RegisterReplacement extends TimelineRequest {
        private final Long originLineupPlayerId;
        private final Long replacementLineupPlayerId;

        public RegisterReplacement(
                Long gameTeamId,
                Long recordedQuarterId,
                Long originLineupPlayerId,
                Long replacementLineupPlayerId,
                Integer recordedAt
        ) {
            super(gameTeamId, recordedQuarterId, recordedAt);
            this.originLineupPlayerId = originLineupPlayerId;
            this.replacementLineupPlayerId = replacementLineupPlayerId;
        }
    }
}
