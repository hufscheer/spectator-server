package com.sports.server.command.leagueteam.domain;

import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface LeagueTeamPlayerFixtureRepository extends Repository<LeagueTeamPlayer, Long> {
    Optional<LeagueTeamPlayer> findById(Long id);
}
