package com.sports.server.query.repository;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameTeamQueryRepository extends Repository<GameTeam, Long> {

    List<GameTeam> findAllByGame(final Game game);

    @Query("select gt from GameTeam gt join fetch gt.team where gt.game = :game")
    List<GameTeam> findAllByGameWithTeam(@Param("game") final Game game);

    @Query("select gt from GameTeam gt "
            + "join fetch gt.team "
            + "where gt.game.id in :gameIds")
    List<GameTeam> findAllByGameIds(final List<Long> gameIds);
}
