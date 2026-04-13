package com.sports.server.command.timeline.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.timeline.domain.TimelineType;
import com.sports.server.command.timeline.domain.WarningCardType;
import com.sports.server.command.timeline.exception.TimelineErrorMessage;
import com.sports.server.common.exception.BadRequestException;
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
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "sportType", visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TimelineRequest.RegisterSoccerScore.class, name = "SOCCER"),
            @JsonSubTypes.Type(value = TimelineRequest.RegisterBasketballScore.class, name = "BASKETBALL")
    })
    public abstract static class RegisterScore extends TimelineRequest {
        private final Long gameTeamId;
        private final Long scoreLineupPlayerId;
        private final Long assistLineupPlayerId;

        protected RegisterScore(
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

        @JsonIgnore
        public abstract int getScoreValue();

        @Override
        public TimelineType getType() {
            return TimelineType.SCORE;
        }
    }

    @Getter
    public static class RegisterSoccerScore extends RegisterScore {
        @JsonCreator
        public RegisterSoccerScore(
                @JsonProperty("gameTeamId") Long gameTeamId,
                @JsonProperty("sportType") SportType sportType,
                @JsonProperty("recordedQuarter") String recordedQuarter,
                @JsonProperty("scoreLineupPlayerId") Long scoreLineupPlayerId,
                @JsonProperty("recordedAt") Integer recordedAt,
                @JsonProperty("assistLineupPlayerId") Long assistLineupPlayerId
        ) {
            super(gameTeamId, sportType, recordedQuarter, scoreLineupPlayerId, recordedAt, assistLineupPlayerId);
        }

        @Override
        public int getScoreValue() {
            return 1;
        }
    }

    @Getter
    public static class RegisterBasketballScore extends RegisterScore {
        private final int score;

        @JsonCreator
        public RegisterBasketballScore(
                @JsonProperty("gameTeamId") Long gameTeamId,
                @JsonProperty("sportType") SportType sportType,
                @JsonProperty("recordedQuarter") String recordedQuarter,
                @JsonProperty("scoreLineupPlayerId") Long scoreLineupPlayerId,
                @JsonProperty("recordedAt") Integer recordedAt,
                @JsonProperty("assistLineupPlayerId") Long assistLineupPlayerId,
                @JsonProperty("score") int score
        ) {
            super(gameTeamId, sportType, recordedQuarter, scoreLineupPlayerId, recordedAt, assistLineupPlayerId);
            if (score != 1 && score != 2 && score != 3) {
                throw new BadRequestException(TimelineErrorMessage.INVALID_BASKETBALL_SCORE);
            }
            this.score = score;
        }

        @Override
        public int getScoreValue() {
            return score;
        }
    }

    @Getter
    public static class RegisterReplacement extends TimelineRequest {
        private final Long gameTeamId;
        private final Long originLineupPlayerId;
        private final Long replacementLineupPlayerId;
        private final Boolean isFoulOut;

        public RegisterReplacement(
                Long gameTeamId,
                SportType sportType,
                String recordedQuarter,
                Long originLineupPlayerId,
                Long replacementLineupPlayerId,
                Integer recordedAt,
                Boolean isFoulOut
        ) {
            super(sportType, recordedQuarter, recordedAt);
            this.gameTeamId = gameTeamId;
            this.originLineupPlayerId = originLineupPlayerId;
            this.replacementLineupPlayerId = replacementLineupPlayerId;
            this.isFoulOut = isFoulOut;
        }

        @Override
        public TimelineType getType() {
            if (getSportType() == SportType.BASKETBALL) {
                return TimelineType.BASKETBALL_REPLACEMENT;
            }
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
        private final Long offenderLineupPlayerId;

        public RegisterFoul(
                Integer recordedAt,
                SportType sportType,
                String recordedQuarter,
                Long gameTeamId,
                Long offenderLineupPlayerId
        ) {
            super(sportType, recordedQuarter, recordedAt);
            this.gameTeamId = gameTeamId;
            this.offenderLineupPlayerId = offenderLineupPlayerId;
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
