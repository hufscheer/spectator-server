package com.sports.server.command.game.domain;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface GameRepository extends Repository<Game, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "SELECT g FROM Game g " +
                    "WHERE g.id = :id"
    )
    Optional<Game> findByIdForUpdate(long id);

    Game save(Game game);

    void delete(Game game);

    @Query(
            "SELECT g FROM Game g " +
                    "WHERE g.startTime <= :cutoffTime " +
                    "AND g.state = 'PLAYING'"
    )
    List<Game> findGamesOlderThanFiveHours(@Param("cutoffTime") LocalDateTime cutoffTime);

    List<Game> findAllByIdIn(List<Long> gameIds);
}
