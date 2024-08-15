package com.sports.server.command.timeline.dto;

import com.sports.server.command.timeline.domain.GameProgressType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimelineRequest {
    private final Long recordedQuarterId;
    private final Integer recordedAt;

    @Getter
    public static class RegisterScore extends TimelineRequest {
        private final Long gameTeamId;
        private final Long scoreLineupPlayerId;

        public RegisterScore(
                Long gameTeamId,
                Long recordedQuarterId,
                Long scoreLineupPlayerId,
                Integer recordedAt
        ) {
            super(recordedQuarterId, recordedAt);
            this.gameTeamId = gameTeamId;
            this.scoreLineupPlayerId = scoreLineupPlayerId;
        }
    }

    @Getter
    public static class RegisterReplacement extends TimelineRequest {
        private final Long gameTeamId;
        private final Long originLineupPlayerId;
        private final Long replacementLineupPlayerId;

        public RegisterReplacement(
                Long gameTeamId,
                Long recordedQuarterId,
                Long originLineupPlayerId,
                Long replacementLineupPlayerId,
                Integer recordedAt
        ) {
            super(recordedQuarterId, recordedAt);
            this.gameTeamId = gameTeamId;
            this.originLineupPlayerId = originLineupPlayerId;
            this.replacementLineupPlayerId = replacementLineupPlayerId;
        }
    }

    @Getter
    public static class RegisterProgress extends TimelineRequest {
        private final GameProgressType gameProgressType;

        public RegisterProgress(
                Integer recordedAt,
                Long recordedQuarterId,
                GameProgressType gameProgressType
        ) {
            super(recordedQuarterId, recordedAt);
            this.gameProgressType = gameProgressType;
        }
    }
}
