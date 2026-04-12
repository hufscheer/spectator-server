package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface GameProgressTimelineRepository extends Repository<GameProgressTimeline, Long> {
    Optional<GameProgressTimeline> findFirstByGameOrderByIdDesc(Game game);
}