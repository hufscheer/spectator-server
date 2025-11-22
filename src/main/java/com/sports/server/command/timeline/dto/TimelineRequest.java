package com.sports.server.command.timeline.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.Quarter;
import com.sports.server.command.timeline.domain.TimelineType;
import com.sports.server.command.timeline.domain.WarningCardType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class TimelineRequest {
    private final Quarter recordedQuarter;
    private final Integer recordedAt;

    @JsonIgnore
    public abstract TimelineType getType();

    @Getter
    public static class RegisterScore extends TimelineRequest {
        private final Long gameTeamId;
        private final Long scoreLineupPlayerId;

        public RegisterScore(
                Long gameTeamId,
                Quarter recordedQuarter,
                Long scoreLineupPlayerId,
                Integer recordedAt
        ) {
            super(recordedQuarter, recordedAt);
            this.gameTeamId = gameTeamId;
            this.scoreLineupPlayerId = scoreLineupPlayerId;
        }

        @Override
        public TimelineType getType() {
            return TimelineType.SCORE;
        }
    }

    @Getter
    public static class RegisterReplacement extends TimelineRequest {
        private final Long gameTeamId;
        private final Long originLineupPlayerId;
        private final Long replacementLineupPlayerId;

        public RegisterReplacement(
                Long gameTeamId,
                Quarter recordedQuarter,
                Long originLineupPlayerId,
                Long replacementLineupPlayerId,
                Integer recordedAt
        ) {
            super(recordedQuarter, recordedAt);
            this.gameTeamId = gameTeamId;
            this.originLineupPlayerId = originLineupPlayerId;
            this.replacementLineupPlayerId = replacementLineupPlayerId;
        }

        @Override
        public TimelineType getType() {
            return TimelineType.REPLACEMENT;
        }
    }

    @Getter
    public static class RegisterProgress extends TimelineRequest {
        private final GameProgressType gameProgressType;

        public RegisterProgress(
                Integer recordedAt,
                Quarter recordedQuarter,
                GameProgressType gameProgressType
        ) {
            super(recordedQuarter, recordedAt);
            this.gameProgressType = gameProgressType;
        }

        @Override
        public TimelineType getType() {
            return TimelineType.GAME_PROGRESS;
        }
    }

    @Getter
    public static class RegisterPk extends TimelineRequest {
        private final Long gameTeamId;
        private final Long scorerId;
        private final Boolean isSuccess;

        public RegisterPk(
                Integer recordedAt,
                Quarter recordedQuarter,
                Long gameTeamId,
                Long scorerId,
                boolean isSuccess
        ) {
            super(recordedQuarter, recordedAt);
            this.gameTeamId = gameTeamId;
            this.scorerId = scorerId;
            this.isSuccess = isSuccess;
        }

        @Override
        public TimelineType getType() {
            return TimelineType.PK;
        }
    }

    @Getter
    public static class RegisterWarningCard extends TimelineRequest {
        private final Long gameTeamId;
        private final Long warnedLineupPlayerId;
        private final WarningCardType cardType;

        public RegisterWarningCard(
                Integer recordedAt,
                Quarter recordedQuarter,
                Long gameTeamId,
                Long warnedLineupPlayerId,
                WarningCardType cardType
        ){
            super(recordedQuarter, recordedAt);
            this.gameTeamId = gameTeamId;
            this.warnedLineupPlayerId = warnedLineupPlayerId;
            this.cardType = cardType;
        }

        @Override
        public TimelineType getType() {
            return TimelineType.WARNING_CARD;
        }
    }
}