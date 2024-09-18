package com.sports.server.support.fixture;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.league.domain.League;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CheerTalkFixtureRepository extends Repository<CheerTalk, Long> {

    @Query("SELECT gt.game.league FROM GameTeam gt " +
            "JOIN CheerTalk ct ON ct.gameTeamId = gt.id " +
            "WHERE ct.id = :cheerTalkId")
    League findLeagueByCheerTalkId(@Param("cheerTalkId") Long cheerTalkId);
}