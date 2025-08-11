package com.sports.server.query.repository;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.League;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface GameQueryRepository extends Repository<Game, Long> {
    @Query(
            "SELECT g FROM Game g "
                    + "JOIN FETCH g.gameTeams "
                    + "WHERE g.league =:league "
                    + "AND g.state = 'PLAYING'"
    )
    List<Game> findPlayingGamesByLeagueWithGameTeams(@Param("league") League league);

    @Query(
            "SELECT g FROM Game g "
                    + "JOIN FETCH g.gameTeams "
                    + "WHERE g.league=:league"
    )
    List<Game> findByLeagueWithGameTeams(@Param("league") League league);

    @Query(
            "SELECT g FROM Game g "
                    + "JOIN FETCH g.league "
                    + "JOIN GameTeam gt ON gt.game = g "
                    + "WHERE gt.id = :gameTeamId"
    )
    Game findByGameTeamIdWithLeague(@Param("gameTeamId") Long gameTeamId);

    @Query(
            "SELECT g FROM Game g "
                    + "JOIN FETCH g.league l "
                    + "WHERE EXISTS (SELECT 1 FROM GameTeam gt WHERE gt.game = g "
                    + "AND gt.team.id = :teamId)"
                    + "ORDER BY g.id DESC ")
    List<Game> findGamesByTeamId(@Param("teamId") Long teamId);
}
