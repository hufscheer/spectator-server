package com.sports.server.command.timeline.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameRepository;
import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.domain.TimelineRepository;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.command.timeline.mapper.TimelineMapper;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.exception.CustomException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;


@Service
@Transactional
@RequiredArgsConstructor
public class TimelineService {
    private final GameRepository gameRepository;
    private final GameTeamRepository gameTeamRepository;
    private final TimelineRepository timelineRepository;
    private final TimelineMapper timelineMapper;
    private final EntityUtils entityUtils;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void register(Member manager, Long gameId, TimelineRequest request) {
        Game game = getGameForUpdate(gameId);
        game.checkStateForTimeline();
        PermissionValidator.checkPermission(game, manager);

        Timeline timeline = timelineMapper.toEntity(game, request);
        timeline.apply();
        timelineRepository.save(timeline);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
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
