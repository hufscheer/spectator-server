package com.sports.server.command.league.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LeagueRepository extends Repository<League, Integer> {
    League save(League league);

    void delete(League league);

    @Query("SELECT l FROM League l LEFT JOIN FETCH l.leagueTeams WHERE l.id = :id")
    Optional<League> findWithTeamsById(@Param("id") Long id);
}
