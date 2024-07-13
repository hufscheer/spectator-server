package com.sports.server.query.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;

public interface LeagueTeamPlayerQueryRepository extends Repository<LeagueTeamPlayer, Long> {
	@Query("select ltp from LeagueTeamPlayer ltp where ltp.leagueTeam.id = :leagueTeamId order by ltp.name asc")
	List<LeagueTeamPlayer> findByLeagueTeamId(@Param("leagueTeamId") Long leagueTeamId);
}
