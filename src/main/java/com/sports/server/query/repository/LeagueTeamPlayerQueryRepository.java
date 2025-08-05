package com.sports.server.query.repository;

import java.util.List;

import com.sports.server.command.league.domain.LeagueTeamPlayer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;


public interface LeagueTeamPlayerQueryRepository extends Repository<LeagueTeamPlayer, Long> {
	@Query("SELECT ltp FROM LeagueTeamPlayer ltp JOIN FETCH ltp.player WHERE ltp.leagueTeam.id = :leagueTeamId")
	List<LeagueTeamPlayer> findByLeagueTeamId(@Param("leagueTeamId") Long leagueTeamId);
}
