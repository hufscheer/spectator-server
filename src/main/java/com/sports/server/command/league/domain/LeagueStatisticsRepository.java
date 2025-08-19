package com.sports.server.command.league.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeagueStatisticsRepository extends Repository<LeagueStatistics, Long> {
    LeagueStatistics save(LeagueStatistics leagueStatistic);

    LeagueStatistics findByLeagueId(Long leagueId);

    @Query("SELECT ls FROM LeagueStatistics ls JOIN FETCH ls.league " +
            "WHERE ls.firstWinnerTeam.id = :teamId OR ls.secondWinnerTeam.id = :teamId")
    List<LeagueStatistics> findTrophiesByTeamId(@Param("teamId") Long teamId);
}
