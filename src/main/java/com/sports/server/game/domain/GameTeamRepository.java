package com.sports.server.game.domain;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameTeamRepository extends Repository<GameTeam, Long> {

    List<GameTeam> findAllByGame(final Game game);

    @Query("select gt from GameTeam gt join fetch gt.team where gt.game = :game")
    List<GameTeam> findAllByGameWithTeam(@Param("game") final Game game);

    @Modifying
    @Query("UPDATE GameTeam t SET t.cheerCount = t.cheerCount + :cheerCount WHERE t.id = :gameTeamId")
    void updateCheerCount(@Param("gameTeamId") Long gameTeamId, @Param("cheerCount") int cheerCount);
}
