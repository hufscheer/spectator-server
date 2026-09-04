package com.sports.server.command.timeline.domain;

import com.sports.server.command.bracket.exception.BracketErrorMessages;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.timeline.exception.TimelineErrorMessage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class TimelineDeletabilityEvaluator {

    private TimelineDeletabilityEvaluator() {
    }

    public enum Reason {
        MIDDLE_DELETE_ONLY_WHILE_PLAYING(TimelineErrorMessage.MIDDLE_DELETE_ONLY_WHILE_PLAYING),
        PROGRESS_TIMELINE_NOT_LAST(TimelineErrorMessage.PROGRESS_TIMELINE_NOT_LAST),
        INCONSISTENT_PROGRESS_STATE(TimelineErrorMessage.INCONSISTENT_PROGRESS_STATE),
        REPLACEMENT_PLAYER_HAS_LATER_RECORDS(TimelineErrorMessage.REPLACEMENT_PLAYER_HAS_LATER_RECORDS),
        SEMI_FINAL_LOCKED_BY_THIRD_PLACE(BracketErrorMessages.SEMI_FINAL_LOCKED_BY_THIRD_PLACE);

        private final String message;

        Reason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public record Result(boolean deletable, Reason reason) {

        public static Result allowed() {
            return new Result(true, null);
        }

        public static Result blocked(Reason reason) {
            return new Result(false, reason);
        }

        public String reasonMessage() {
            return reason == null ? null : reason.getMessage();
        }

        public String reasonCode() {
            return reason == null ? null : reason.name();
        }
    }

    public static Map<Long, Result> evaluate(GameState gameState, List<Timeline> timelines,
                                             boolean semiFinalLockedByThirdPlace) {
        List<Timeline> ordered = timelines.stream()
                .sorted(Comparator.comparing(Timeline::getId))
                .toList();

        Map<Long, Result> results = new HashMap<>();
        Set<Long> laterPlayerIds = new HashSet<>();
        boolean gameEndLater = false;

        for (int i = ordered.size() - 1; i >= 0; i--) {
            Timeline timeline = ordered.get(i);
            boolean isLast = i == ordered.size() - 1;

            results.put(timeline.getId(), isLast
                    ? evaluateLast(timeline, semiFinalLockedByThirdPlace)
                    : evaluateMiddle(gameState, timeline, gameEndLater, laterPlayerIds));

            gameEndLater = gameEndLater || timeline.isGameEnd();
            timeline.getRelatedLineupPlayers().stream()
                    .map(LineupPlayer::getId)
                    .forEach(laterPlayerIds::add);
        }
        return results;
    }

    private static Result evaluateLast(Timeline timeline, boolean semiFinalLockedByThirdPlace) {
        if (semiFinalLockedByThirdPlace && timeline.isGameEnd()) {
            return Result.blocked(Reason.SEMI_FINAL_LOCKED_BY_THIRD_PLACE);
        }
        return Result.allowed();
    }

    public static Result evaluateMiddle(GameState gameState, Timeline target, List<Timeline> subsequents) {
        boolean gameEndLater = subsequents.stream().anyMatch(Timeline::isGameEnd);
        Set<Long> laterPlayerIds = subsequents.stream()
                .flatMap(timeline -> timeline.getRelatedLineupPlayers().stream())
                .map(LineupPlayer::getId)
                .collect(Collectors.toSet());
        return evaluateMiddle(gameState, target, gameEndLater, laterPlayerIds);
    }

    private static Result evaluateMiddle(GameState gameState, Timeline target,
                                         boolean gameEndLater, Set<Long> laterPlayerIds) {
        if (gameState != GameState.PLAYING) {
            return Result.blocked(Reason.MIDDLE_DELETE_ONLY_WHILE_PLAYING);
        }
        if (target instanceof GameProgressTimeline) {
            return Result.blocked(Reason.PROGRESS_TIMELINE_NOT_LAST);
        }
        if (gameEndLater) {
            return Result.blocked(Reason.INCONSISTENT_PROGRESS_STATE);
        }
        if (target instanceof ReplacementTimeline
                && target.getRelatedLineupPlayers().stream()
                .anyMatch(player -> laterPlayerIds.contains(player.getId()))) {
            return Result.blocked(Reason.REPLACEMENT_PLAYER_HAS_LATER_RECORDS);
        }
        return Result.allowed();
    }
}
