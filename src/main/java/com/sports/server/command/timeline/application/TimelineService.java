package com.sports.server.command.timeline.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.timeline.TimelineDto;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.TimelineRepository;
import com.sports.server.common.application.EntityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class TimelineService {
    private final TimelineRepository timelineRepository;
    private final EntityUtils entityUtils;

    public void registerScoreTimeline(Long gameId, TimelineDto.RegisterScore request) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        Quarter quarter = entityUtils.getEntity(request.recordedQuarterId(), Quarter.class);
        LineupPlayer scorer = entityUtils.getEntity(request.scoreLineupPlayerId(), LineupPlayer.class);

        ScoreTimeline timeline = ScoreTimeline.score(
                game,
                quarter,
                request.recordedAt(),
                scorer
        );

        timelineRepository.save(timeline);
    }
}
