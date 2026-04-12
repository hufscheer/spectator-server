package com.sports.server.query.application;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.GameProgressTimelineRepository;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.AvailableProgressResponse;
import com.sports.server.query.dto.response.AvailableProgressResponse.ProgressAction;
import com.sports.server.query.dto.response.TimelineResponse;
import com.sports.server.query.repository.TimelineQueryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimelineQueryService {

    private final TimelineQueryRepository timelineQueryRepository;
    private final GameProgressTimelineRepository gameProgressTimelineRepository;
    private final EntityUtils entityUtils;

    public List<TimelineResponse> getTimelines(final Long gameId) {
        Map<Quarter, List<Timeline>> timelines = timelineQueryRepository.findByGameId(gameId)
                .stream()
                .collect(groupingBy(Timeline::getRecordedQuarter));

        return timelines.keySet()
                .stream()
                .sorted(comparingInt(Quarter::getOrder).reversed())
                .map(quarter -> TimelineResponse.of(
                        quarter,
                        timelines.get(quarter)
                )).toList();
    }

    public AvailableProgressResponse getAvailableProgress(Long gameId) {
        Game game = entityUtils.getEntity(gameId, Game.class);

        if (game.getState() == GameState.FINISHED) {
            return new AvailableProgressResponse(List.of());
        }

        Optional<GameProgressTimeline> lastOpt = gameProgressTimelineRepository.findFirstByGameOrderByIdDesc(game);
        return new AvailableProgressResponse(computeActions(game.getLeague().getSportType(), lastOpt));
    }

    private List<ProgressAction> computeActions(SportType sportType, Optional<GameProgressTimeline> lastOpt) {
        if (lastOpt.isEmpty()) {
            return List.of(ProgressAction.of(sportType.firstQuarter(), GameProgressType.QUARTER_START));
        }

        Quarter lastQuarter = lastOpt.get().getRecordedQuarter();
        GameProgressType lastType = lastOpt.get().getGameProgressType();

        return switch (lastType) {
            case QUARTER_START -> actionsFromQuarterStart(lastQuarter);
            case QUARTER_END -> actionsFromQuarterEnd(lastQuarter, sportType);
            default -> List.of();
        };
    }

    private List<ProgressAction> actionsFromQuarterStart(Quarter lastQuarter) {
        List<ProgressAction> actions = new ArrayList<>();
        if (lastQuarter.canHaveQuarterEnd()) {
            actions.add(ProgressAction.of(lastQuarter, GameProgressType.QUARTER_END));
        }
        if (lastQuarter.canEndGame()) {
            actions.add(ProgressAction.of(lastQuarter, GameProgressType.GAME_END));
        }
        return actions;
    }

    private List<ProgressAction> actionsFromQuarterEnd(Quarter lastQuarter, SportType sportType) {
        List<ProgressAction> actions = new ArrayList<>();
        Quarter nextQuarter = sportType.nextQuarter(lastQuarter);
        actions.add(ProgressAction.of(nextQuarter, GameProgressType.QUARTER_START));
        if (lastQuarter.canEndGameAfterQuarterEnd()) {
            actions.add(ProgressAction.of(lastQuarter, GameProgressType.GAME_END));
        }
        return actions;
    }
}
