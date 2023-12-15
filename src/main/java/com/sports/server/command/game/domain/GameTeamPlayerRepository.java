package com.sports.server.command.game.domain;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameTeamPlayerRepository extends Repository<GameTeamPlayer, Long> {

    @Query("select gtp from GameTeamPlayer gtp " +
            "join fetch gtp.gameTeam gt " +
            "join fetch gt.team " +
            "where gt.game.id = :gameId")
    List<GameTeamPlayer> findPlayersByGameId(@Param("gameId") final Long gameId);
}
