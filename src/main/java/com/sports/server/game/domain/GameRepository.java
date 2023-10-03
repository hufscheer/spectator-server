package com.sports.server.game.domain;

import org.springframework.data.repository.Repository;

public interface GameRepository extends Repository<Game, Long> {
    void save(final Game game);
}
