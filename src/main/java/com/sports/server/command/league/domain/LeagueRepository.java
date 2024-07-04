package com.sports.server.command.league.domain;

import org.springframework.data.repository.Repository;

public interface LeagueRepository extends Repository<League, Integer> {
	void save(League league);
}
