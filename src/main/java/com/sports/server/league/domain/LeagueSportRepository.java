package com.sports.server.league.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface LeagueSportRepository extends Repository<LeagueSport, Long> {

    @EntityGraph(attributePaths = "sport")
    List<LeagueSport> findByLeagueId(Long leagueId);
}
