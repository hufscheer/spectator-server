package com.sports.server.support.fixture;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameTeamFixtureRepository extends JpaRepository<GameTeam, Long> {
    @Query("SELECT gt FROM GameTeam gt " +
            "JOIN FETCH gt.team " +
            "LEFT JOIN FETCH gt.lineupPlayers lp " +
            "LEFT JOIN FETCH lp.player " +
            "WHERE gt.game = :game")
    List<GameTeam> findByGame(@Param("game") Game game);
}
