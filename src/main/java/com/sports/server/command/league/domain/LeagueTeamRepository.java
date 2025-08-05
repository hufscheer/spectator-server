package com.sports.server.command.league.domain;

import com.sports.server.command.team.domain.Team;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface LeagueTeamRepository extends Repository<LeagueTeam, Integer> {
    void save(LeagueTeam leagueTeam);

    void delete(LeagueTeam leagueTeam);

    boolean existsByLeagueIdAndTeamId(Long leagueId, Long teamId);

    Optional<LeagueTeam> findByLeagueIdAndTeamId(Long leagueId, Long teamId);

    Optional<LeagueTeam> findByLeagueAndTeam(League league, Team team);
}
