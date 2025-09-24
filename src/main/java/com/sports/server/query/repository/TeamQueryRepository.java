package com.sports.server.query.repository;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamQueryRepository extends Repository<Team, Long> {
    List<Team> findAll();

    @Query("SELECT tp FROM TeamPlayer tp JOIN FETCH tp.player WHERE tp.team.id = :teamId")
    List<TeamPlayer> findTeamPlayersByTeamId(@Param("teamId") Long teamId);
}
