package com.sports.server.command.cheertalk.domain;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CheerTalkRepository extends Repository<CheerTalk, Long> {
    void save(CheerTalk cheerTalk);

    @Query("SELECT COUNT(ct) FROM CheerTalk ct " +
           "JOIN GameTeam gt ON ct.gameTeamId = gt.id " +
           "JOIN gt.game g " +
           "WHERE gt.team.id = :teamId AND g.league.id = :leagueId AND ct.blockStatus = com.sports.server.command.cheertalk.domain.CheerTalkBlockStatus.ACTIVE")
    Long countCheerTalksByTeamIdAndLeagueId(@Param("teamId") Long teamId, @Param("leagueId") Long leagueId);
}
