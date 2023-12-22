package com.sports.server.query.repository;

import com.sports.server.command.league.domain.LeagueSport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface LeagueSportQueryRepository extends Repository<LeagueSport, Long> {

    @EntityGraph(attributePaths = "sport")
    List<LeagueSport> findByLeagueId(Long leagueId);
}
