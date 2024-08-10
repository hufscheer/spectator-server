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
                    + "JOIN FETCH g.teams "
                    + "WHERE g.league =:league "
                    + "AND g.state = 'PLAYING'"
    )
    List<Game> findByLeagueWithGameTeams(@Param("league") League league);
}
