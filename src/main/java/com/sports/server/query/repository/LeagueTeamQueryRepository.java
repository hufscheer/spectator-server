package com.sports.server.query.repository;

import com.sports.server.command.league.domain.LeagueTeam;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeagueTeamQueryRepository extends Repository<LeagueTeam, Long> {
    @Query("SELECT lt FROM LeagueTeam lt WHERE lt.league.id = :leagueId")
    List<LeagueTeam> findByLeagueId(@Param("leagueId") Long leagueId);
}
