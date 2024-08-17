package com.sports.server.command.game.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface GameTeamRepository extends Repository<GameTeam, Long> {

    @Modifying
    @Query("UPDATE GameTeam t SET t.cheerCount = t.cheerCount + :cheerCount WHERE t.id = :gameTeamId")
    void updateCheerCount(@Param("gameTeamId") Long gameTeamId, @Param("cheerCount") int cheerCount);

    @Query(
            "SELECT gt FROM GameTeam gt "
                    + "JOIN FETCH gt.lineupPlayers "
                    + "WHERE gt.id=:id "
    )
    Optional<GameTeam> findGameTeamByIdWithLineupPlayers(Long id);
}
