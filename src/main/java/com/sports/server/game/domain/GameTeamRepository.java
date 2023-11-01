package com.sports.server.game.domain;

import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface GameTeamRepository extends Repository<GameTeam, Long> {

    Optional<GameTeam> findById(final Long id);
}
