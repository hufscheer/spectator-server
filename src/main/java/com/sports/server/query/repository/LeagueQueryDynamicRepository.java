package com.sports.server.query.repository;

import com.sports.server.command.league.domain.League;

import java.util.List;

public interface LeagueQueryDynamicRepository {

    List<League> findByYear(Integer year);
}
