package com.sports.server.query.repository;

import com.sports.server.command.player.domain.Player;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface PlayerQueryRepository extends Repository<Player, Long> {
    List<Player> findAll();
}
