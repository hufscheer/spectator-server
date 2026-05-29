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
                    "JOIN FETCH g.league " +
                    "WHERE g.id = :id"
    )
    Optional<Game> findByIdForUpdate(long id);

    Game save(Game game);

    void delete(Game game);

    @Query(
            "SELECT DISTINCT g FROM Game g " +
                    "JOIN FETCH g.league " +
                    "LEFT JOIN FETCH g.gameTeams " +
                    "WHERE g.startTime <= :cutoffTime " +
                    "AND g.state = 'PLAYING'"
    )
    List<Game> findGamesOlderThanFiveHours(@Param("cutoffTime") LocalDateTime cutoffTime);

    List<Game> findAllByIdIn(List<Long> gameIds);

    @Query("SELECT g FROM Game g " +
           "JOIN FETCH g.league l " +
           "WHERE g.state = 'SCHEDULED' " +
           "AND l.sportType = 'SOCCER' " +
           "AND g.startTime BETWEEN :from AND :to")
    List<Game> findScheduledSoccerGamesBetween(@Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    @Query("SELECT g FROM Game g JOIN FETCH g.league WHERE g.id = :id")
    Optional<Game> findByIdWithLeague(@Param("id") Long id);
}
