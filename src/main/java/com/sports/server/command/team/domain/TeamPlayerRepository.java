package com.sports.server.command.team.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, Long> {
    @Query("SELECT tp.player.id FROM TeamPlayer tp WHERE tp.team.id = :teamId")
    List<Long> findPlayerIdsByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT tp FROM TeamPlayer tp JOIN FETCH tp.player WHERE tp.team.id = :teamId")
    List<TeamPlayer> findTeamPlayersWithPlayerByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT tp FROM TeamPlayer tp JOIN FETCH tp.player WHERE tp.id IN :ids")
    List<TeamPlayer> findAllByTeamPlayerIds(@Param("ids") List<Long> teamPlayerIds);

    @Query("SELECT tp FROM TeamPlayer tp JOIN FETCH tp.team WHERE tp.player.id = :playerId")
    List<TeamPlayer> findAllByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT tp FROM TeamPlayer tp JOIN FETCH tp.player JOIN FETCH tp.team WHERE tp.player.id IN :playerIds")
    List<TeamPlayer> findAllByPlayerIds(@Param("playerIds") List<Long> playerIds);
}
