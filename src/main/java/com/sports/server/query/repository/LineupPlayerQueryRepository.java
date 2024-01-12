package com.sports.server.query.repository;


import com.sports.server.command.game.domain.LineupPlayer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LineupPlayerQueryRepository extends Repository<LineupPlayer, Long> {

    @Query("select lp from LineupPlayer lp " +
            "join fetch lp.gameTeam gt " +
            "join fetch gt.leagueTeam " +
            "where gt.game.id = :gameId")
    List<LineupPlayer> findPlayersByGameId(@Param("gameId") final Long gameId);
}
