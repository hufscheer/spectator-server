package com.sports.server.command.timeline.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.command.timeline.domain.ReplacementTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.domain.TimelineRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class TimelineService {
    private final TimelineRepository timelineRepository;
    private final EntityUtils entityUtils;

    public void registerScore(Member member, Long gameId, TimelineRequest.RegisterScore request) {
        ScoreTimeline timeline = ScoreTimeline.score(
                checkPermissionAndGet(gameId, member),
                getQuarter(request.getRecordedQuarterId()),
                request.getRecordedAt(),
                getPlayer(request.getScoreLineupPlayerId())
        );

        timelineRepository.save(timeline);
    }

    public void registerReplacement(Member member, Long gameId, TimelineRequest.RegisterReplacement request) {
        ReplacementTimeline timeline = new ReplacementTimeline(
                checkPermissionAndGet(gameId, member),
                getQuarter(request.getRecordedQuarterId()),
                request.getRecordedAt(),
                getPlayer(request.getOriginLineupPlayerId()),
                getPlayer(request.getReplacementLineupPlayerId())
        );

        timelineRepository.save(timeline);
    }

    public void deleteTimeline(Member member, Long gameId, Long timelineId) {
        Game game = checkPermissionAndGet(gameId, member);

        Timeline timeline = getLastTimeline(timelineId, game);

        timeline.rollback();
        timelineRepository.delete(timeline);
    }

    private Timeline getLastTimeline(Long timelineId, Game game) {
        return timelineRepository.findFirstByGameOrderByIdDesc(game)
                .filter(t -> t.getId().equals(timelineId))
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "마지막 타임라인만 삭제할 수 있습니다."));
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
