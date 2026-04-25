package com.sports.server.query.repository;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.Unit;

import java.util.List;

public interface TeamQueryDynamicRepository {

    List<LeagueTeam> findByLeagueAndRound(League league, Integer roundNumber);

    List<Team> findAllByUnitsAndSportType(List<Unit> units, SportType sportType, Long organizationId);

    List<Unit> findDistinctUnitsBySportTypeAndOrganizationId(SportType sportType, Long organizationId);
}
