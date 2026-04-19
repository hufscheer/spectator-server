package com.sports.server.command.timeline.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameRepository;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.GameProgressTimelineRepository;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.domain.TimelineRepository;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.command.timeline.exception.TimelineErrorMessage;
import com.sports.server.command.timeline.mapper.TimelineMapper;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.exception.CustomException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class TimelineService {
    private final GameRepository gameRepository;
    private final TimelineRepository timelineRepository;
    private final GameProgressTimelineRepository gameProgressTimelineRepository;
    private final TimelineMapper timelineMapper;
    private final EntityUtils entityUtils;

    @Transactional
    public void register(Member manager, Long gameId, TimelineRequest request) {
        Game game = getGameForUpdate(gameId);
        game.checkStateForTimeline();
        PermissionValidator.checkPermission(game, manager);

        if (request instanceof TimelineRequest.RegisterProgress progressRequest) {
            validateProgressTransition(game, progressRequest);

            if (progressRequest.getGameProgressType() == GameProgressType.GAME_END) {
                insertQuarterEndIfNeeded(game, progressRequest.getRecordedAt());
            }
        }

        Timeline timeline = timelineMapper.toEntity(game, request);
        timeline.apply();
        timelineRepository.save(timeline);
    }

    private void insertQuarterEndIfNeeded(Game game, Integer recordedAt) {
        gameProgressTimelineRepository.findFirstByGameOrderByIdDesc(game)
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

    private void validateProgressTransition(Game game, TimelineRequest.RegisterProgress request) {
        Quarter requestQuarter = request.resolveQuarter();
        GameProgressType requestType = request.getGameProgressType();
        Optional<GameProgressTimeline> lastOpt = gameProgressTimelineRepository.findFirstByGameOrderByIdDesc(game);

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
        Game game = entityUtils.getEntity(gameId, Game.class);
        PermissionValidator.checkPermission(game, manager);

        Timeline timeline = getLastTimeline(timelineId, game);
        timeline.rollback();
        timelineRepository.delete(timeline);
    }

    private Game getGameForUpdate(Long id) {
        return gameRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 게임입니다."));
    }

    private Timeline getLastTimeline(Long timelineId, Game game) {
        return timelineRepository.findFirstByGameOrderByIdDesc(game).filter(t -> t.getId().equals(timelineId))
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "마지막 타임라인만 삭제할 수 있습니다."));
    }
}
