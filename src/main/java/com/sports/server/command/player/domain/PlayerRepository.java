package com.sports.server.command.player.domain;


import org.springframework.data.repository.Repository;

import java.util.List;

public interface PlayerRepository extends Repository<Player, Long> {
    void save(Player player);

    void delete(Player player);

    boolean existsByStudentNumber(String studentNumber);

    List<Player> findAllById(Iterable<Long> ids);
}
