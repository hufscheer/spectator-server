package com.sports.server.command.league.domain;

import com.sports.server.command.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeagueTeamRepository extends JpaRepository<LeagueTeam, Long> {
    Optional<LeagueTeam> findByLeagueAndTeam(League league, Team team);

    @Query("SELECT lt.team.id FROM LeagueTeam lt WHERE lt.league.id = :leagueId AND lt.team.id IN :teamIds")
    List<Long> findTeamIdsByLeagueIdAndTeamIdIn(@Param("leagueId") Long leagueId, @Param("teamIds") List<Long> teamIds);

    @Query("SELECT lt FROM LeagueTeam lt JOIN FETCH lt.team " +
            "WHERE lt.league.id = :leagueId AND lt.team.id IN :teamIds")
    List<LeagueTeam> findAllByLeagueAndTeamIdsIn(@Param("leagueId") Long leagueId, @Param("teamIds") List<Long> teamIds);

    @Query("SELECT lt.team.id FROM LeagueTeam lt WHERE lt.league.id = :leagueId")
    List<Long> findTeamIdsByLeagueId(@Param("leagueId") Long leagueId);
}
