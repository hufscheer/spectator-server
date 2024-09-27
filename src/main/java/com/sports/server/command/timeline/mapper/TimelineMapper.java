package com.sports.server.command.timeline.mapper;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.PKTimeline;
import com.sports.server.command.timeline.domain.ReplacementTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.domain.TimelineType;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.common.application.EntityUtils;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimelineMapper {
    private final EntityUtils entityUtils;

    private final Map<TimelineType, TimelineSupplier> suppliers = Map.of(
            TimelineType.SCORE, (g, r) -> toScoreTimeline(g, (TimelineRequest.RegisterScore) r),
            TimelineType.REPLACEMENT, (g, r) -> toReplacementTimeline(g, (TimelineRequest.RegisterReplacement) r),
            TimelineType.GAME_PROGRESS, (g, r) -> toProgressTimeline(g, (TimelineRequest.RegisterProgress) r),
            TimelineType.PK, (g, r) -> toPkTimeline(g, (TimelineRequest.RegisterPk) r)
    );

    public Timeline toEntity(Game game, TimelineRequest request) {
        return Optional.ofNullable(suppliers.get(request.getType()))
                .map(supplier -> supplier.get(game, request))
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 타입입니다."));
    }

    private ScoreTimeline toScoreTimeline(Game game,
                                          TimelineRequest.RegisterScore scoreRequest) {
        return ScoreTimeline.score(
                game,
                getQuarter(scoreRequest.getRecordedQuarterId()),
                scoreRequest.getRecordedAt(),
                getPlayer(scoreRequest.getScoreLineupPlayerId())
        );
    }

    private ReplacementTimeline toReplacementTimeline(Game game,
                                                      TimelineRequest.RegisterReplacement replacementRequest) {
        return new ReplacementTimeline(
                game,
                getQuarter(replacementRequest.getRecordedQuarterId()),
                replacementRequest.getRecordedAt(),
                getPlayer(replacementRequest.getOriginLineupPlayerId()),
                getPlayer(replacementRequest.getReplacementLineupPlayerId())
        );
    }

    private Timeline toProgressTimeline(Game game,
                                        TimelineRequest.RegisterProgress progressRequest) {
        return new GameProgressTimeline(
                game,
                getQuarter(progressRequest.getRecordedQuarterId()),
                progressRequest.getRecordedAt(),
                progressRequest.getGameProgressType()
        );
    }

    private PKTimeline toPkTimeline(Game game,
                                    TimelineRequest.RegisterPk pkRequest) {
        return new PKTimeline(
                game,
                getQuarter(pkRequest.getRecordedQuarterId()),
                pkRequest.getRecordedAt(),
                getPlayer(pkRequest.getScorerId()),
                pkRequest.getIsSuccess()
        );
    }

    private Quarter getQuarter(Long quarterId) {
        return entityUtils.getEntity(quarterId, Quarter.class);
    }

    private LineupPlayer getPlayer(Long playerId) {
        return entityUtils.getEntity(playerId, LineupPlayer.class);
    }
}
