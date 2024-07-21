package com.sports.server.command.timeline.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.timeline.TimelineDto;
import com.sports.server.command.timeline.domain.ReplacementTimeline;
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
        ScoreTimeline timeline = ScoreTimeline.score(
                checkPermissionAndGet(gameId, member),
                getQuarter(request.recordedQuarterId()),
                request.recordedAt(),
                getPlayer(request.scoreLineupPlayerId())
        );

        timelineRepository.save(timeline);
    }

    public void registerReplacement(Member member, Long gameId, TimelineDto.RegisterReplacement request) {
        ReplacementTimeline timeline = new ReplacementTimeline(
                checkPermissionAndGet(gameId, member),
                getQuarter(request.recordedQuarterId()),
                request.recordedAt(),
                getPlayer(request.originLineupPlayerId()),
                getPlayer(request.replacementLineupPlayerId())
        );

        timelineRepository.save(timeline);
    }

    private Game checkPermissionAndGet(Long gameId, Member member) {
        Game game = entityUtils.getEntity(gameId, Game.class);

        if (!game.isMangedBy(member)) {
            throw new UnauthorizedException("타임라인 생성 권한이 없습니다.");
        }

        return game;
    }

    private Quarter getQuarter(Long quarterId) {
        return entityUtils.getEntity(quarterId, Quarter.class);
    }

    private LineupPlayer getPlayer(Long playerId) {
        return entityUtils.getEntity(playerId, LineupPlayer.class);
    }
}
