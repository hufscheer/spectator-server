package com.sports.server.command.game.domain;

import com.sports.server.command.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineupPlayerRepository extends JpaRepository<LineupPlayer, Long> {
    boolean existsByGameTeamAndPlayer(GameTeam gameTeam, Player player);
}
