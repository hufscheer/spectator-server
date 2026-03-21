package com.sports.server.query.repository;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CheerTalkQueryRepository extends Repository<CheerTalk, Long> {

    @Query("SELECT COUNT(ct) " +
           "FROM CheerTalk ct " +
           "JOIN GameTeam gt ON ct.gameTeamId = gt.id " +
           "WHERE gt.game.league.id = :leagueId " +
           "AND ct.blockStatus = com.sports.server.command.cheertalk.domain.CheerTalkBlockStatus.ACTIVE")
    Long countActiveCheerTalksByLeagueId(@Param("leagueId") Long leagueId);
}