package com.sports.server.query.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.sports.server.command.leagueteam.LeagueTeam;

public interface LeagueTeamQueryRepository extends Repository<LeagueTeam, Long> {
    @Query("select lt from LeagueTeam lt where lt.league.id = :leagueId order by lt.name asc")
    List<LeagueTeam> findByLeagueId(@Param("leagueId") final Long leagueId);


}
