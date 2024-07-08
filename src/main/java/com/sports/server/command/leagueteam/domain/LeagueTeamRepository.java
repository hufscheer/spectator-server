package com.sports.server.command.leagueteam.domain;

import org.springframework.data.repository.Repository;

public interface LeagueTeamRepository extends Repository<LeagueTeam, Long> {
    void save(LeagueTeam leagueTeam);
}
