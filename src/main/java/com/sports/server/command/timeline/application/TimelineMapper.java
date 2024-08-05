package com.sports.server.command.timeline.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.timeline.domain.ReplacementTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.common.application.EntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimelineMapper {
    private final EntityUtils entityUtils;

    public Timeline toEntity(Game game, TimelineRequest request) {
        if (request instanceof TimelineRequest.RegisterScore scoreRequest) {
            return toScoreTimeline(game, scoreRequest);
        } else if (request instanceof TimelineRequest.RegisterReplacement replacementRequest) {
            return toReplacementTimeline(game, replacementRequest);
        }

        throw new IllegalArgumentException("지원하지 않는 타입입니다.");
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

    private Quarter getQuarter(Long quarterId) {
        return entityUtils.getEntity(quarterId, Quarter.class);
    }

    private LineupPlayer getPlayer(Long playerId) {
        return entityUtils.getEntity(playerId, LineupPlayer.class);
    }
}
