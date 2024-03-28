package com.sports.server.query.repository;

import com.sports.server.command.record.domain.ScoreRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScoreRecordQueryRepository extends Repository<ScoreRecord, Long> {

    @Query("select sr from ScoreRecord sr " +
            "join fetch sr.record r " +
            "join fetch r.gameTeam gt " +
            "join fetch gt.leagueTeam lt " +
            "join fetch r.recordedQuarter rq " +
            "join fetch sr.lineupPlayer srp " +
            "where r.game.id = :gameId " +
            "order by r.id asc")
    List<ScoreRecord> findByGameId(@Param("gameId") Long gameId);
}
