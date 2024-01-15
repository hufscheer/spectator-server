package com.sports.server.query.repository;

import com.sports.server.command.record.domain.Record;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecordQueryRepository extends Repository<Record, Long> {

    @Query("select r from Record r " +
            "join fetch r.game " +
            "join fetch r.gameTeam rgt " +
            "join fetch rgt.leagueTeam " +
            "join fetch r.lineupPlayer " +
            "join fetch r.scoredQuarter rq " +
            "where r.game.id = :gameId " +
            "order by rq.id desc, r.scoredAt desc")
    List<Record> findByGameIdOrderByQuarterAndScoredAtDesc(final @Param("gameId") Long gameId);
}
