package com.sports.server.command.timeline.mapper;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.timeline.domain.*;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.common.application.EntityUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimelineMapper {
    private final EntityUtils entityUtils;

    private final Map<TimelineType, TimelineSupplier> suppliers = Map.of(
            TimelineType.SCORE,
            (game, gameTeams, request) -> toScoreTimeline(game, (TimelineRequest.RegisterScore) request),
            TimelineType.REPLACEMENT,
            (game, gameTeams, request) -> toReplacementTimeline(game, (TimelineRequest.RegisterReplacement) request),
            TimelineType.GAME_PROGRESS,
            (game, gameTeams, request) -> toProgressTimeline(game, (TimelineRequest.RegisterProgress) request),
            TimelineType.PK, (game, gameTeams, request) -> toPkTimeline(game, (TimelineRequest.RegisterPk) request),
            TimelineType.WARNING_CARD,
            (game, gameTeams, request) -> toWarningCardTimeline(game, (TimelineRequest.RegisterWarningCard) request)
    );

    public Timeline toEntity(Game game, List<GameTeam> gameTeams, TimelineRequest request) {
        return Optional.ofNullable(suppliers.get(request.getType()))
                .map(supplier -> supplier.get(game, gameTeams, request))
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 타입입니다."));
    }

    private ScoreTimeline toScoreTimeline(Game game,
                                          TimelineRequest.RegisterScore scoreRequest) {
        return ScoreTimeline.score(
                game,
                scoreRequest.getRecordedQuarter(),
                scoreRequest.getRecordedAt(),
                getPlayer(scoreRequest.getScoreLineupPlayerId())
        );
    }

    private ReplacementTimeline toReplacementTimeline(Game game,
                                                      TimelineRequest.RegisterReplacement replacementRequest) {
        return new ReplacementTimeline(
                game,
                replacementRequest.getRecordedQuarter(),
                replacementRequest.getRecordedAt(),
                getPlayer(replacementRequest.getOriginLineupPlayerId()),
                getPlayer(replacementRequest.getReplacementLineupPlayerId())
        );
    }

    private Timeline toProgressTimeline(Game game,
                                        TimelineRequest.RegisterProgress progressRequest) {
        return new GameProgressTimeline(
                game,
                progressRequest.getRecordedQuarter(),
                progressRequest.getRecordedAt(),
                progressRequest.getGameProgressType()
        );
    }

    private PKTimeline toPkTimeline(Game game,
                                    TimelineRequest.RegisterPk pkRequest) {
        return new PKTimeline(
                game,
                pkRequest.getRecordedQuarter(),
                pkRequest.getRecordedAt(),
                getPlayer(pkRequest.getScorerId()),
                pkRequest.getIsSuccess()
        );
    }

    private WarningCardTimeline toWarningCardTimeline(Game game,
                                                      TimelineRequest.RegisterWarningCard warningCardRequest) {
        return new WarningCardTimeline(
                game,
                warningCardRequest.getRecordedQuarter(),
                warningCardRequest.getRecordedAt(),
                getPlayer(warningCardRequest.getWarnedLineupPlayerId()),
                warningCardRequest.getCardType()
        );
    }

    private LineupPlayer getPlayer(Long playerId) {
        return entityUtils.getEntity(playerId, LineupPlayer.class);
    }
}