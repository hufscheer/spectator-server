package com.sports.server.command.leagueteam.domain;

import org.springframework.data.repository.Repository;

public interface LeagueTeamPlayerRepository extends Repository<LeagueTeamPlayer, Long> {
    void delete(LeagueTeamPlayer leagueTeamPlayer);
}
