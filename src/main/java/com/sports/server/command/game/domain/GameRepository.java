package com.sports.server.command.game.domain;

import org.springframework.data.repository.Repository;

public interface GameRepository extends Repository<Game, Long> {
    Game save(Game game);
    void delete(Game game);
}
