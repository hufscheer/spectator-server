package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TimelineRepository extends Repository<Timeline, Long> {
    void save(Timeline timeline);

    Optional<Timeline> findFirstByGameOrderByIdDesc(Game game);

    void delete(Timeline timeline);

    @Modifying
    @Query("DELETE FROM Timeline t WHERE t.game = :game")
    void deleteByGame(@Param("game") Game game);
}
