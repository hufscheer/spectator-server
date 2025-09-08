package com.sports.server.command.league.domain;

import com.sports.server.command.league.dto.LeagueTeamStats;
import com.sports.server.command.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeagueTeamRepository extends JpaRepository<LeagueTeam, Long> {
    @Query("SELECT lt FROM LeagueTeam lt WHERE lt.league = :league AND lt.team = :team")
    Optional<LeagueTeam> findByLeagueAndTeam(@Param("league") League league, @Param("team") Team team);

    @Query("SELECT lt.team.id FROM LeagueTeam lt WHERE lt.league.id = :leagueId AND lt.team.id IN :teamIds")
    List<Long> findTeamIdsByLeagueIdAndTeamIdIn(@Param("leagueId") Long leagueId, @Param("teamIds") List<Long> teamIds);

    @Query("SELECT lt FROM LeagueTeam lt JOIN FETCH lt.team " +
            "WHERE lt.league.id = :leagueId AND lt.team.id IN :teamIds")
    List<LeagueTeam> findAllByLeagueAndTeamIdsIn(@Param("leagueId") Long leagueId, @Param("teamIds") List<Long> teamIds);

    @Query("SELECT lt.team.id FROM LeagueTeam lt WHERE lt.league.id = :leagueId")
    List<Long> findTeamIdsByLeagueId(@Param("leagueId") Long leagueId);

    List<LeagueTeam> findByLeagueId(Long id);

    @Query("SELECT new com.sports.server.command.league.dto.LeagueTeamStats(" +
           "lt.id, " +
           "COALESCE(" +
           "  (SELECT SUM(CAST(gt2.cheerCount AS long)) " +
           "   FROM GameTeam gt2 " +
           "   JOIN gt2.game g2 " +
           "   WHERE gt2.team.id = lt.team.id AND g2.league.id = :leagueId), 0L), " +
           "COALESCE(" +
           "  (SELECT COUNT(ct2.id) " +
           "   FROM CheerTalk ct2 " +
           "   JOIN GameTeam gt3 ON ct2.gameTeamId = gt3.id " +
           "   JOIN gt3.game g3 " +
           "   WHERE gt3.team.id = lt.team.id AND g3.league.id = :leagueId AND ct2.isBlocked = false), 0L)) " +
           "FROM LeagueTeam lt " +
           "WHERE lt.league.id = :leagueId")
    List<LeagueTeamStats> findLeagueTeamStatsWithCounts(@Param("leagueId") Long leagueId);
}
