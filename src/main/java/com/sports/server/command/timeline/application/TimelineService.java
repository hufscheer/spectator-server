package com.sports.server.command.timeline.application;

import com.sports.server.command.game.application.GameStatusScheduler;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameRepository;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.GameProgressTimelineRepository;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.ReplacementTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.domain.TimelineCreatedEvent;
import com.sports.server.command.timeline.domain.TimelineRepository;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.command.timeline.exception.TimelineErrorMessage;
import com.sports.server.command.timeline.mapper.TimelineMapper;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.exception.CustomException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TimelineService {
    private final GameRepository gameRepository;
    private final TimelineRepository timelineRepository;
    private final GameProgressTimelineRepository gameProgressTimelineRepository;
    private final TimelineMapper timelineMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final GameStatusScheduler gameStatusScheduler;

    @Transactional
    public void register(Member manager, Long gameId, TimelineRequest request) {
        Game game = getGameForUpdate(gameId);
        game.checkStateForTimeline();
        PermissionValidator.checkPermission(game, manager);

        if (request instanceof TimelineRequest.RegisterProgress progressRequest) {
            Optional<GameProgressTimeline> lastProgressOpt =
                    gameProgressTimelineRepository.findFirstByGameOrderByIdDesc(game);
            validateProgressTransition(game, progressRequest, lastProgressOpt);

            if (progressRequest.getGameProgressType() == GameProgressType.GAME_END) {
                insertQuarterEndIfNeeded(game, progressRequest.getRecordedAt(), lastProgressOpt);
            }
        }

        Timeline timeline = timelineMapper.toEntity(game, request);
        timeline.apply();
        timelineRepository.save(timeline);

        if (timeline.isGameEnd()) {
            gameStatusScheduler.updateLeagueStatisticsIfNeeded(game);
        }

        eventPublisher.publishEvent(new TimelineCreatedEvent(
                timeline.getId(), gameId, timeline.getType()));
    }

    private void insertQuarterEndIfNeeded(Game game, Integer recordedAt, Optional<GameProgressTimeline> lastProgressOpt) {
        lastProgressOpt
                .filter(last -> last.getGameProgressType() == GameProgressType.QUARTER_START
                        && last.getRecordedQuarter().canHaveQuarterEnd())
                .ifPresent(last -> {
                    GameProgressTimeline quarterEnd = new GameProgressTimeline(
                            game,
                            last.getRecordedQuarter(),
                            recordedAt,
                            GameProgressType.QUARTER_END
                    );
                    quarterEnd.apply();
                    timelineRepository.save(quarterEnd);
                });
    }

    private void validateProgressTransition(Game game, TimelineRequest.RegisterProgress request,
                                            Optional<GameProgressTimeline> lastOpt) {
        Quarter requestQuarter = request.resolveQuarter();
        GameProgressType requestType = request.getGameProgressType();

        boolean isValid = lastOpt
                .map(last -> isValidTransitionFrom(last.getRecordedQuarter(), last.getGameProgressType(), requestQuarter, requestType))
                .orElseGet(() -> isValidFirstTransition(game, requestQuarter, requestType));

        if (!isValid) {
            throw new CustomException(HttpStatus.BAD_REQUEST, TimelineErrorMessage.INVALID_PROGRESS_TRANSITION);
        }
    }

    private boolean isValidFirstTransition(Game game, Quarter requestQuarter, GameProgressType requestType) {
        return requestType == GameProgressType.QUARTER_START
                && requestQuarter.equals(game.getLeague().getSportType().firstQuarter());
    }

    private boolean isValidTransitionFrom(Quarter lastQuarter, GameProgressType lastType, Quarter requestQuarter, GameProgressType requestType) {
        return switch (lastType) {
            case QUARTER_START -> isValidFromQuarterStart(lastQuarter, requestQuarter, requestType);
            case QUARTER_END -> isValidFromQuarterEnd(lastQuarter, requestQuarter, requestType);
            default -> false;
        };
    }

    private boolean isValidFromQuarterStart(Quarter lastQuarter, Quarter requestQuarter, GameProgressType requestType) {
        return switch (requestType) {
            case QUARTER_END -> lastQuarter.canHaveQuarterEnd() && requestQuarter.equals(lastQuarter);
            case GAME_END -> lastQuarter.canEndGame();
            default -> false;
        };
    }

    private boolean isValidFromQuarterEnd(Quarter lastQuarter, Quarter requestQuarter, GameProgressType requestType) {
        if (requestType == GameProgressType.GAME_END) {
            return lastQuarter.canEndGame();
        }
        return requestType == GameProgressType.QUARTER_START
                && requestQuarter.getOrder() == lastQuarter.getOrder() + 1;
    }

    @Transactional
    public void deleteTimeline(Member manager, Long gameId, Long timelineId) {
        Game game = getGameForUpdate(gameId);
        PermissionValidator.checkPermission(game, manager);

        Timeline timeline = getTimeline(timelineId, game);
        List<Timeline> subsequents = timelineRepository.findByGameAndIdGreaterThanOrderByIdAsc(game, timelineId);

        if (subsequents.isEmpty()) {
            deleteLastTimeline(game, timeline);
            return;
        }
        deleteMiddleTimeline(game, timeline, subsequents);
    }

    private void deleteLastTimeline(Game game, Timeline timeline) {
        boolean isGameEnd = timeline.isGameEnd();
        timeline.rollback();
        timelineRepository.delete(timeline);

        if (isGameEnd) {
            gameStatusScheduler.rollbackLeagueStatisticsIfNeeded(game);
        }
    }

    private void deleteMiddleTimeline(Game game, Timeline target, List<Timeline> subsequents) {
        validateMiddleDeletable(game, target, subsequents);

        for (int i = subsequents.size() - 1; i >= 0; i--) {
            rollbackWithUnderflowLog(subsequents.get(i));
        }
        rollbackWithUnderflowLog(target);
        timelineRepository.delete(target);
        subsequents.forEach(Timeline::apply);
    }

    private void validateMiddleDeletable(Game game, Timeline target, List<Timeline> subsequents) {
        if (game.getState() != GameState.PLAYING) {
            throw new CustomException(HttpStatus.BAD_REQUEST, TimelineErrorMessage.MIDDLE_DELETE_ONLY_WHILE_PLAYING);
        }
        if (target instanceof GameProgressTimeline) {
            throw new CustomException(HttpStatus.BAD_REQUEST, TimelineErrorMessage.PROGRESS_TIMELINE_NOT_LAST);
        }
        if (subsequents.stream().anyMatch(Timeline::isGameEnd)) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, TimelineErrorMessage.INCONSISTENT_PROGRESS_STATE);
        }
        if (target instanceof ReplacementTimeline replacement) {
            validateReplacementNotReferenced(replacement, subsequents);
        }
    }

    private void validateReplacementNotReferenced(ReplacementTimeline target, List<Timeline> subsequents) {
        Set<Long> replacementPlayerIds = target.getRelatedLineupPlayers().stream()
                .map(LineupPlayer::getId)
                .collect(Collectors.toSet());

        boolean referenced = subsequents.stream()
                .flatMap(timeline -> timeline.getRelatedLineupPlayers().stream())
                .anyMatch(player -> replacementPlayerIds.contains(player.getId()));

        if (referenced) {
            throw new CustomException(HttpStatus.BAD_REQUEST, TimelineErrorMessage.REPLACEMENT_PLAYER_HAS_LATER_RECORDS);
        }
    }

    private void rollbackWithUnderflowLog(Timeline timeline) {
        if (timeline instanceof ScoreTimeline scoreTimeline) {
            GameTeam scorerTeam = scoreTimeline.getScorer().getGameTeam();
            if (scorerTeam.getScore() < scoreTimeline.getScore()) {
                log.warn("타임라인 replay 롤백 중 점수 언더플로 감지 — 데이터 정합 확인 필요. timelineId={}, teamScore={}, rollbackScore={}",
                        timeline.getId(), scorerTeam.getScore(), scoreTimeline.getScore());
            }
        }
        timeline.rollback();
    }

    private Game getGameForUpdate(Long id) {
        return gameRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, TimelineErrorMessage.GAME_NOT_FOUND));
    }

    private Timeline getTimeline(Long timelineId, Game game) {
        return timelineRepository.findById(timelineId)
                .filter(timeline -> timeline.getGame().getId().equals(game.getId()))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, TimelineErrorMessage.TIMELINE_NOT_FOUND));
    }
}
