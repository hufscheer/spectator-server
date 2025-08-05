package com.sports.server.command.team.domain;

import org.springframework.data.repository.Repository;

public interface TeamRepository extends Repository<Team, Long> {
    void save(Team team);
    void delete(Team team);
}
