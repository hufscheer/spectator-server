package com.sports.server.command.team.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Set;


public interface TeamPlayerRepository extends Repository<TeamPlayer, Long> {
    void save(TeamPlayer teamPlayer);

    void delete(TeamPlayer teamPlayer);

    @Query("SELECT tp.player.id FROM TeamPlayer tp WHERE tp.team = :team")
    Set<Long> findPlayerIdsByTeam(@Param("team") Team team);
}
