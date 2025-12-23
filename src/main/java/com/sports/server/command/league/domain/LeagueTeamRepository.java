package com.sports.server.command.league.domain;

import com.sports.server.command.league.dto.LeagueTeamStats;
import com.sports.server.command.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Query("UPDATE LeagueTeam lt SET lt.totalCheerCount = " +
           "COALESCE((SELECT CAST(SUM(gt.cheerCount) AS int) " +
           "FROM GameTeam gt JOIN gt.game g " +
           "WHERE gt.team.id = lt.team.id AND g.league.id = :leagueId), 0) " +
           "WHERE lt.league.id = :leagueId")
    void updateTotalCheerCounts(@Param("leagueId") Long leagueId);

    @Modifying
    @Query("UPDATE LeagueTeam lt SET lt.totalTalkCount = " +
           "COALESCE((SELECT CAST(COUNT(ct.id) AS int) " +
           "FROM CheerTalk ct, GameTeam gt2 " +
           "WHERE ct.gameTeamId = gt2.id AND gt2.team.id = lt.team.id " +
           "AND gt2.game.league.id = :leagueId AND ct.blockStatus = com.sports.server.command.cheertalk.domain.CheerTalkBlockStatus.ACTIVE), 0) " +
           "WHERE lt.league.id = :leagueId")
    void updateTotalTalkCounts(@Param("leagueId") Long leagueId);

    @Query("SELECT new com.sports.server.command.league.dto.LeagueTeamStats(" +
           "lt.id, CAST(lt.totalCheerCount AS long), CAST(lt.totalTalkCount AS long)) " +
           "FROM LeagueTeam lt WHERE lt.league.id = :leagueId")
    List<LeagueTeamStats> findLeagueTeamStatsWithCounts(@Param("leagueId") Long leagueId);
}
