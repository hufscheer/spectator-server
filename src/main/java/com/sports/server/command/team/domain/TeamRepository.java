package com.sports.server.command.team.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TeamRepository extends Repository<Team, Long> {
    Team save(Team team);

    void delete(Team team);

    List<Team> findAllById(Iterable<Long> ids);

    List<Team> findAll();

    long countByIdIn(Collection<Long> ids);

    @Query("SELECT t.id FROM Team t WHERE t.id IN :ids")
    List<Long> findExistingIds(@Param("ids") Collection<Long> ids);
}
