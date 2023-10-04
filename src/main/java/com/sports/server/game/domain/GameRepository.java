package com.sports.server.game.domain;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends Repository<Game, Long> {
    Long save(final Game game);

    Optional<Game> findById(final Long id);

    List<Game> findAll();
}
