package com.sports.server.command.league.domain;

import org.springframework.data.repository.Repository;

public interface LeagueStatisticRepository extends Repository<LeagueStatistic, Long> {
    LeagueStatistic save(LeagueStatistic leagueStatistic);
    LeagueStatistic findByLeagueId(Long leagueId);
}

