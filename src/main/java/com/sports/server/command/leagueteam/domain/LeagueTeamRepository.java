package com.sports.server.command.leagueteam.domain;

import com.sports.server.command.league.domain.League;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface LeagueTeamRepository extends Repository<LeagueTeam, Long> {
    void save(LeagueTeam leagueTeam);

    Optional<LeagueTeam> findByLeagueAndName(League league, String name);
}
