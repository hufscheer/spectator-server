package com.sports.server.command.league.domain;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;


public interface LeagueRepository extends Repository<League, Integer> {
    League save(League league);

    void delete(League league);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM League l WHERE l.id = :id")
    Optional<League> findByIdForUpdate(@Param("id") Long id);
}
