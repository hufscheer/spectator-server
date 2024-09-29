package com.sports.server.command.game.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LineUpPlayerRepository extends Repository<LineupPlayer, Long> {
    @Query("select lp from LineupPlayer lp join fetch lp.gameTeam where lp.id = :id")
    LineupPlayer findWithGameTeam(@Param("id") final Long id);
}
