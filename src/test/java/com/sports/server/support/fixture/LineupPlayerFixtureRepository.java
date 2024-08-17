package com.sports.server.support.fixture;

import com.sports.server.command.game.domain.LineupPlayer;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface LineupPlayerFixtureRepository extends Repository<LineupPlayer, Long> {
    Optional<LineupPlayer> findById(Long id);
}
