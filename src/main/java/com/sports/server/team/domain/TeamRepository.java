package com.sports.server.team.domain;

import com.sports.server.team.domain.Team;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends Repository<Team, Long> {

    Optional<Team> findById(final Long id);

    List<Team> findAll();
}