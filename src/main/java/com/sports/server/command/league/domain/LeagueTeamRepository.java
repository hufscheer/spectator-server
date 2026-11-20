package com.sports.server.command.league.domain;

import com.sports.server.command.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT new com.sports.server.command.league.domain.LeagueTeamRef(lt.id, cast(fk(lt.team) as long)) " +
            "FROM LeagueTeam lt WHERE fk(lt.league) = :leagueId")
    List<LeagueTeamRef> findRefsByLeagueId(@Param("leagueId") Long leagueId);

    // 종료된 경기만 센다. 잠금 없는 읽기라 진행 중 경기의 응원 카운트 갱신을 막지 않는다
    @Query("SELECT new com.sports.server.command.league.domain.TeamTotal(" +
            "cast(fk(gt.team) as long), cast(SUM(gt.cheerCount) as long)) " +
            "FROM GameTeam gt JOIN gt.game g " +
            "WHERE fk(g.league) = :leagueId AND g.state = com.sports.server.command.game.domain.GameState.FINISHED " +
            "GROUP BY fk(gt.team)")
    List<TeamTotal> sumCheerCountsOfFinishedGames(@Param("leagueId") Long leagueId);

    @Query("SELECT new com.sports.server.command.league.domain.TeamTotal(" +
            "cast(fk(gt.team) as long), count(ct.id)) " +
            "FROM CheerTalk ct JOIN GameTeam gt ON ct.gameTeamId = gt.id JOIN gt.game g " +
            "WHERE fk(g.league) = :leagueId AND g.state = com.sports.server.command.game.domain.GameState.FINISHED " +
            "AND ct.blockStatus = com.sports.server.command.cheertalk.domain.CheerTalkBlockStatus.ACTIVE " +
            "GROUP BY fk(gt.team)")
    List<TeamTotal> countActiveCheerTalksOfFinishedGames(@Param("leagueId") Long leagueId);
}
