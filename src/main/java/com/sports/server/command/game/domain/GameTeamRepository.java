package com.sports.server.command.game.domain;

import com.sports.server.query.application.TeamGameResult;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameTeamRepository extends Repository<GameTeam, Long> {
    void save(GameTeam gameTeam);

    @Modifying
    @Query("UPDATE GameTeam t SET t.cheerCount = t.cheerCount + :cheerCount WHERE t.id = :gameTeamId")
    void updateCheerCount(@Param("gameTeamId") Long gameTeamId, @Param("cheerCount") int cheerCount);

    @Query("SELECT SUM(gt.cheerCount) FROM GameTeam gt " +
           "JOIN gt.game g " +
           "WHERE gt.team.id = :teamId AND g.league.id = :leagueId")
    Long sumCheerCountByTeamIdAndLeagueId(@Param("teamId") Long teamId, @Param("leagueId") Long leagueId);

    @Query("SELECT DISTINCT gt FROM GameTeam gt " +
            "JOIN FETCH gt.team " +
            "JOIN FETCH gt.game " +
            "WHERE gt.game.id IN :gameIds")
    List<GameTeam> findAllByGameIds(@Param("gameIds") List<Long> gameIds);

    @Query("SELECT new com.sports.server.query.application.TeamGameResult(gt.team.id, gt.result, COUNT(gt)) " +
            "FROM GameTeam gt " +
            "WHERE gt.team.id IN :teamIds AND gt.result IS NOT NULL " +
            "GROUP BY gt.team.id, gt.result")
    List<TeamGameResult> findGameResultsByTeamIds(@Param("teamIds") List<Long> teamIds);
}
