package com.sports.server.query.repository;


import com.sports.server.command.game.domain.LineupPlayer;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LineupPlayerQueryRepository extends Repository<LineupPlayer, Long> {

    @Query("select lp from LineupPlayer lp " +
            "join fetch lp.gameTeam gt " +
            "join fetch gt.leagueTeam " +
            "where gt.game.id = :gameId " +
            "order by lp.number asc")
    List<LineupPlayer> findPlayersByGameId(@Param("gameId") final Long gameId);

    @Query("select lp from LineupPlayer lp " +
            "join fetch lp.gameTeam gt " +
            "join fetch gt.leagueTeam " +
            "where gt.game.id = :gameId " +
            "and lp.isPlaying = true " +
            "order by lp.number asc")
    List<LineupPlayer> findPlayingPlayersByGameId(@Param("gameId") final Long gameId);
}
