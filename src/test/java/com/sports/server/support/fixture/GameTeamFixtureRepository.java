package com.sports.server.support.fixture;

import com.sports.server.command.game.domain.GameTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameTeamFixtureRepository extends JpaRepository<GameTeam, Long> {
}
