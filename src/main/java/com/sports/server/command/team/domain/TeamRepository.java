package com.sports.server.command.team.domain;

import org.springframework.data.repository.Repository;

import java.util.List;

public interface TeamRepository extends Repository<Team, Long> {
    Team save(Team team);

    void delete(Team team);

    List<Team> findAllById(Iterable<Long> ids);
}
