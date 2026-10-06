package com.sports.server.command.league.domain;

import org.springframework.data.repository.Repository;


public interface LeagueRepository extends Repository<League, Integer> {
    League save(League league);

    void delete(League league);
}
