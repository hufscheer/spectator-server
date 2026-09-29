package com.sports.server.query.repository;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameResult;
import com.sports.server.command.game.domain.GameTeam;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameTeamQueryRepository extends Repository<GameTeam, Long> {

    List<GameTeam> findAllByGame(final Game game);

    // left join 이라 지워진 팀(@Where)은 team 이 null 로 채워진다
    @Query("select gt from GameTeam gt "
            + "left join fetch gt.team "
            + "where gt.game.id = :gameId")
    List<GameTeam> findAllByGameIdWithTeam(@Param("gameId") Long gameId);

    @Query("select gt from GameTeam gt "
            + "join fetch gt.team "
            + "join fetch gt.game "
            + "where gt.game.id in :gameIds")
    List<GameTeam> findAllByGameIds(@Param("gameIds") final List<Long> gameIds);

    @Query("select gt from GameTeam gt "
            + "join fetch gt.team "
            + "where gt.game.id = :gameId and gt.result = :result")
    Optional<GameTeam> findByGameIdAndResult(@Param("gameId") Long gameId, @Param("result") GameResult result);
}
