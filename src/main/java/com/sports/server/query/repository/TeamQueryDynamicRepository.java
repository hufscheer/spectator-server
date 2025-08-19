package com.sports.server.query.repository;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.Unit;

import java.util.List;

public interface TeamQueryDynamicRepository {

    List<Team> findByLeagueAndRound(League league, Integer roundNumber);

    List<Team> findAllByUnits(List<Unit> units);
}
