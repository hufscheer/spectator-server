package com.sports.server.game.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface GameRepository extends Repository<Game, Long> {

    Optional<Game> findById(final Long id);

    List<Game> findAll();
}
