package com.sports.server.command.timeline.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.timeline.TimelineDto;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.TimelineRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class TimelineService {
    private final TimelineRepository timelineRepository;
    private final EntityUtils entityUtils;

    public void registerScore(Member member, Long gameId, TimelineDto.RegisterScore request) {
        Game game = entityUtils.getEntity(gameId, Game.class);

        if (!game.isMangedBy(member)) {
            throw new UnauthorizedException("타임라인 생성 권한이 없습니다.");
        }

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

    public void registerReplacement(Member member, Long gameId, TimelineDto.RegisterReplacement request) {

    }
}
