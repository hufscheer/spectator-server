package com.sports.server.support.fixture;

import com.sports.server.command.game.domain.LineupPlayer;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;


public interface LineupPlayerFixtureRepository extends Repository<LineupPlayer, Long> {

    @Query("select lp from LineupPlayer lp " +
            "join fetch lp.gameTeam gt " +
            "where gt.game.id = :gameId " +
            "and lp.isPlaying = true ")
    List<LineupPlayer> findPlayingPlayersByGameId(Long gameId);
}
