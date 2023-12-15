package com.sports.server.command.league.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface LeagueRepository extends Repository<League, Long> {

    @Query("select l from League l order by l.startAt desc, l.endAt desc")
    List<League> findAll();
}
