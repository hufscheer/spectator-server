package com.sports.server.command.game.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LineupPlayerRepository extends Repository<LineupPlayer, Long> {
	@Modifying
	@Query("Update LineupPlayer lp set lp.state = :state where lp.id in :ids")
	void updatePlayerState(@Param("state") LineupPlayerState state, @Param("ids") List<Long> ids);
}
