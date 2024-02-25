package com.sports.server.query.repository;

import com.sports.server.command.record.domain.ReplacementRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplacementRecordQueryRepository extends Repository<ReplacementRecord, Long> {

    @Query("select rr from ReplacementRecord rr " +
            "join fetch rr.record r " +
            "join fetch rr.originLineupPlayer rop " +
            "join fetch rr.replacedLineupPlayer rrp " +
            "join fetch r.gameTeam gt " +
            "join fetch gt.leagueTeam lt " +
            "join fetch r.recordedQuarter rq " +
            "where r.game.id = :gameId " +
            "order by rq.id desc, r.recordedAt desc")
    List<ReplacementRecord> findByGameId(@Param("gameId") Long gameId);
}
