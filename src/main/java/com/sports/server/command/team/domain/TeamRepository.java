package com.sports.server.command.team.domain;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends Repository<Team, Long> {

    Optional<Team> findById(final Long id);

    List<Team> findAll();
}