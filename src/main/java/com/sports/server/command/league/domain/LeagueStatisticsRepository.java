package com.sports.server.command.league.domain;

import org.springframework.data.repository.Repository;

public interface LeagueStatisticsRepository extends Repository<LeagueStatistics, Long> {
    LeagueStatistics save(LeagueStatistics leagueStatistic);
    LeagueStatistics findByLeagueId(Long leagueId);
}
