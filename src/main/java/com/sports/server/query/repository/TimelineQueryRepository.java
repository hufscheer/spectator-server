package com.sports.server.query.repository;

import com.sports.server.command.timeline.domain.Timeline;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface TimelineQueryRepository extends Repository<Timeline, Long> {

    @Query("select t from Timeline t " +
            "join fetch t.game g " +
            "join fetch t.recordedQuarter rq " +
            "where t.game.id = :gameId " +
            "order by t.id desc")
    List<Timeline> findByGameId(Long gameId);
}
