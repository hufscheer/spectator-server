package com.sports.server.command.league.domain;

import com.sports.server.command.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeagueTeamRepository extends JpaRepository<LeagueTeam, Long> {
    @Query("SELECT lt FROM LeagueTeam lt WHERE fk(lt.league) = :#{#league.id} AND fk(lt.team) = :#{#team.id}")
    Optional<LeagueTeam> findByLeagueAndTeam(@Param("league") League league, @Param("team") Team team);

    @Query("SELECT cast(fk(lt.team) as long) FROM LeagueTeam lt WHERE fk(lt.league) = :leagueId AND fk(lt.team) IN :teamIds")
    List<Long> findTeamIdsByLeagueIdAndTeamIdIn(@Param("leagueId") Long leagueId, @Param("teamIds") List<Long> teamIds);

    @Query("SELECT lt FROM LeagueTeam lt JOIN FETCH lt.team " +
            "WHERE fk(lt.league) = :leagueId AND fk(lt.team) IN :teamIds")
    List<LeagueTeam> findAllByLeagueAndTeamIdsIn(@Param("leagueId") Long leagueId, @Param("teamIds") List<Long> teamIds);

    @Query("SELECT cast(fk(lt.team) as long) FROM LeagueTeam lt WHERE fk(lt.league) = :leagueId")
    List<Long> findTeamIdsByLeagueId(@Param("leagueId") Long leagueId);

    @Query("SELECT lt FROM LeagueTeam lt WHERE fk(lt.league) = :leagueId")
    List<LeagueTeam> findByLeagueId(@Param("leagueId") Long leagueId);

    @Modifying
    @Query("UPDATE LeagueTeam lt SET lt.totalCheerCount = " +
           "COALESCE((SELECT CAST(SUM(gt.cheerCount) AS int) " +
           "FROM GameTeam gt JOIN gt.game g " +
           "WHERE fk(gt.team) = fk(lt.team) AND fk(g.league) = :leagueId), 0) " +
           "WHERE fk(lt.league) = :leagueId")
    void updateTotalCheerCounts(@Param("leagueId") Long leagueId);

    @Modifying
    @Query("UPDATE LeagueTeam lt SET lt.totalTalkCount = " +
           "COALESCE((SELECT CAST(COUNT(ct.id) AS int) " +
           "FROM CheerTalk ct, GameTeam gt2 " +
           "WHERE ct.gameTeamId = gt2.id AND fk(gt2.team) = fk(lt.team) " +
           "AND fk(gt2.game.league) = :leagueId AND ct.blockStatus = com.sports.server.command.cheertalk.domain.CheerTalkBlockStatus.ACTIVE), 0) " +
           "WHERE fk(lt.league) = :leagueId")
    void updateTotalTalkCounts(@Param("leagueId") Long leagueId);
}
