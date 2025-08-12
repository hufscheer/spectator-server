package com.sports.server.query.repository;

import com.sports.server.command.league.domain.LeagueStatistics;
import org.springframework.data.repository.Repository;

public interface LeagueStatisticsQueryRepository extends Repository<LeagueStatistics, Long> {
    LeagueStatistics findByLeagueId(Long leagueId);
}
