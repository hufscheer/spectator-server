package com.sports.server.game.domain;

import java.util.List;
import org.springframework.data.repository.Repository;

public interface GameTeamRepository extends Repository<GameTeam, Long> {

    List<GameTeam> findAllByGame(final Game game);

    Optional<GameTeam> findById(final Long id);
}
