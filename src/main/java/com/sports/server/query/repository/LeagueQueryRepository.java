package com.sports.server.query.repository;

import com.sports.server.command.league.domain.League;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface LeagueQueryRepository extends Repository<League, Long>, LeagueQueryDynamicRepository {

    Optional<League> findById(Long id);
}
