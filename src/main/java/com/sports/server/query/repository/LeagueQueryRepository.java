package com.sports.server.query.repository;

import com.sports.server.command.league.domain.League;
import org.springframework.data.repository.Repository;

public interface LeagueQueryRepository extends Repository<League, Long>, LeagueQueryDynamicRepository {
}
