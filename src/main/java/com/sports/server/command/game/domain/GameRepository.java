package com.sports.server.command.game.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface GameRepository extends Repository<Game, Long> {
    Game save(Game game);

    void delete(Game game);

    @Query(
            "SELECT g FROM Game g " +
                    "WHERE g.startTime <= :cutoffTime " +
                    "AND g.state = 'PLAYING'"
    )
    List<Game> findGamesOlderThanFiveHours(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT g FROM Game g WHERE g.id IN :gameIds")
    List<Game> findAllByIdIn(@Param("gameIds") List<Long> gameIds);
}
