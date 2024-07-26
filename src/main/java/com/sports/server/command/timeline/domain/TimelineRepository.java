package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface TimelineRepository extends Repository<Timeline, Long> {
    void save(Timeline timeline);

    Optional<Timeline> findFirstByGameOrderByIdDesc(Game game);

    void delete(Timeline timeline);
}
