package com.sports.server.query.repository;

import com.sports.server.command.team.domain.Team;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface TeamQueryRepository extends Repository<Team, Long> {
    List<Team> findAll();
}
