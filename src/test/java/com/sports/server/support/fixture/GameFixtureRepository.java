package com.sports.server.support.fixture;

import com.sports.server.command.game.domain.Game;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface GameFixtureRepository extends Repository<Game, Long> {
    Optional<Game> findByName(String name);

    void save(Game game);
}