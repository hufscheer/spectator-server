package com.sports.server.command.timeline.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.timeline.domain.TimelineType;
import com.sports.server.command.timeline.domain.WarningCardType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class TimelineRequest {
    private final SportType sportType;
    private final String recordedQuarter;
    private final Integer recordedAt;

    @JsonIgnore
    public abstract TimelineType getType();

    public Quarter resolveQuarter() {
        return sportType.resolveQuarter(recordedQuarter);
    }

    @Getter
    public static class RegisterScore extends TimelineRequest {
        private final Long gameTeamId;
        private final Long scoreLineupPlayerId;
        private final Long assistLineupPlayerId;

        public RegisterScore(
                Long gameTeamId,
                SportType sportType,
                String recordedQuarter,
                Long scoreLineupPlayerId,
                Integer recordedAt,
                Long assistLineupPlayerId
        ) {
            super(sportType, recordedQuarter, recordedAt);
            this.gameTeamId = gameTeamId;
            this.scoreLineupPlayerId = scoreLineupPlayerId;
            this.assistLineupPlayerId = assistLineupPlayerId;
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
                SportType sportType,
                String recordedQuarter,
                Long originLineupPlayerId,
                Long replacementLineupPlayerId,
                Integer recordedAt
        ) {
            super(sportType, recordedQuarter, recordedAt);
            this.gameTeamId = gameTeamId;
            this.originLineupPlayerId = originLineupPlayerId;
            this.replacementLineupPlayerId = replacementLineupPlayerId;
        }

        @Override
        public TimelineType getType() {
            return TimelineType.SOCCER_REPLACEMENT;
        }
    }

    @Getter
    public static class RegisterProgress extends TimelineRequest {
        private final GameProgressType gameProgressType;

        public RegisterProgress(
                Integer recordedAt,
                SportType sportType,
                String recordedQuarter,
                GameProgressType gameProgressType
        ) {
            super(sportType, recordedQuarter, recordedAt == null ? 0 : recordedAt);
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
                SportType sportType,
                String recordedQuarter,
                Long gameTeamId,
                Long scorerId,
                boolean isSuccess
        ) {
            super(sportType, recordedQuarter, recordedAt);
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
    public static class RegisterFoul extends TimelineRequest {
        private final Long gameTeamId;
        private final Long fouledLineupPlayerId;

        public RegisterFoul(
                Integer recordedAt,
                SportType sportType,
                String recordedQuarter,
                Long gameTeamId,
                Long fouledLineupPlayerId
        ) {
            super(sportType, recordedQuarter, recordedAt);
            this.gameTeamId = gameTeamId;
            this.fouledLineupPlayerId = fouledLineupPlayerId;
        }

        @Override
        public TimelineType getType() {
            return TimelineType.FOUL;
        }
    }

    @Getter
    public static class RegisterWarningCard extends TimelineRequest {
        private final Long gameTeamId;
        private final Long warnedLineupPlayerId;
        private final WarningCardType cardType;

        public RegisterWarningCard(
                Integer recordedAt,
                SportType sportType,
                String recordedQuarter,
                Long gameTeamId,
                Long warnedLineupPlayerId,
                WarningCardType cardType
        ) {
            super(sportType, recordedQuarter, recordedAt);
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
