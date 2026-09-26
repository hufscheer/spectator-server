package com.sports.server.command.player.domain;


import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends Repository<Player, Long> {
    void save(Player player);

    void delete(Player player);

    List<Player> findAllById(Iterable<Long> ids);

    Optional<Player> findByStudentNumberAndOrganizationId(String studentNumber, Long organizationId);

    List<Player> findByStudentNumberInAndOrganizationId(List<String> studentNumbers, Long organizationId);
}
