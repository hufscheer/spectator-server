package com.sports.server.command.league.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LeagueStatisticsRepository extends Repository<LeagueStatistics, Long> {
    LeagueStatistics save(LeagueStatistics leagueStatistic);

    @Query("SELECT ls FROM LeagueStatistics ls WHERE ls.league.id = :leagueId")
    LeagueStatistics findByLeagueId(@Param("leagueId") Long leagueId);
}
